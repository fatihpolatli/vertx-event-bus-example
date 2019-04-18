package com.example.starter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.netty.channel.MessageSizeEstimator.Handle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

  private Map<Integer, Whisky> products = new LinkedHashMap<>();

  JDBCClient jdbc = null;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    config().put("url", "jdbc:hsqldb:file:db/whiskies");
    config().put("driver_class", "org.hsqldb.jdbcDriver");
    jdbc = JDBCClient.createShared(vertx, config(), "My-Whisky-Collection");

    startBackend((connection) -> createSomeData(connection,
        (nothing) -> startWebApp((http) -> completeStartup(http, startFuture)), startFuture), startFuture);

  }

  private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
    if (http.succeeded()) {
      fut.complete();
    } else {
      fut.fail(http.cause());
    }
  }

  @Override
  public void stop() throws Exception {
    // Close the JDBC client.
    jdbc.close();
  }

  private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
    // Create a router object.
    Router router = Router.router(vertx);

    // Bind "/" to our hello message.
    router.route("/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/html").end("<h1>Hello from my first Vert.x 3 application</h1>");
    });

    // router.route("/assets/*").handler(StaticHandler.create("assets"));

    router.get("/api/whiskies").handler(this::getAll);
    /*
     * router.route("/api/whiskies*").handler(BodyHandler.create());
     * router.post("/api/whiskies").handler(this::addOne);
     * router.get("/api/whiskies/:id").handler(this::getOne);
     * router.put("/api/whiskies/:id").handler(this::updateOne);
     * router.delete("/api/whiskies/:id").handler(this::deleteOne);
     */
    // Create the HTTP server and pass the "accept" method to the request handler.
    vertx.createHttpServer().requestHandler(router::accept).listen(
        // Retrieve the port from the configuration,
        // default to 8080.
        config().getInteger("http.port", 8080), next::handle);

   
  }
  

  private void getAll(RoutingContext routingContext) {

    vertx.eventBus().publish("hello", "Yay! Someone kicked a ball");

    DeliveryOptions options = new DeliveryOptions();
options.addHeader("some-header", "some-value");
vertx.eventBus().send("hello", "Yay! Someone kicked a ball", options);


    vertx.eventBus().<JsonObject>send("hello", "test", reply -> {
      if (reply.succeeded()) {
        System.out.println("Received: " + reply.result().body());
      } else {
        // No reply or failure
        reply.cause().printStackTrace();
      }
    });
    jdbc.getConnection(ar -> {
      SQLConnection connection = ar.result();
      connection.query("SELECT * FROM Whisky", result -> {
        List whiskies = result.result().getRows().stream().collect(Collectors.toList());

        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(whiskies));
        connection.close(); // Close the connection

      });
    });
  }

  private void startBackend(Handler<AsyncResult<SQLConnection>> next, Future<Void> fut) {
    System.out.println("JDBC is " + jdbc);

    jdbc.getConnection(ar -> {
      if (ar.failed()) {
        System.out.println("JDBC is FAILED");
        fut.fail(ar.cause());
      } else {

        System.out.println("JDBC is SUCCESS");
        next.handle(Future.succeededFuture(ar.result()));
      }
    });
  }

  private void createSomeData(AsyncResult<SQLConnection> result, Handler<AsyncResult<Void>> next, Future<Void> fut) {
    if (result.failed()) {
      fut.fail(result.cause());
    } else {
      SQLConnection connection = result.result();
      connection.execute(
          "CREATE TABLE IF NOT EXISTS Whisky (id INTEGER IDENTITY, name varchar(100), " + "origin varchar(100))",
          ar -> {
            if (ar.failed()) {
              fut.fail(ar.cause());
              connection.close();
              return;
            }
            connection.query("SELECT * FROM Whisky", select -> {
              if (select.failed()) {
                fut.fail(ar.cause());
                connection.close();
                return;
              }
              if (select.result().getNumRows() == 0) {
                insert(new Whisky("Bowmore 155 Years Laimrig", "Scotland, Islay"), connection,
                    (v) -> insert(new Whisky("Talisker 557° North", "Scotland, Island"), connection, (r) -> {
                      next.handle(Future.<Void>succeededFuture());
                      connection.close();
                    }));
              } else {
                next.handle(Future.<Void>succeededFuture());
                connection.close();
              }
            });
          });
    }

  }

  private void insert(Whisky whisky, SQLConnection connection, Handler<AsyncResult<Whisky>> next) {
    String sql = "INSERT INTO Whisky (name, origin) VALUES ?, ?";
    connection.updateWithParams(sql, new JsonArray().add(whisky.getName()).add(whisky.getOrigin()), (ar) -> {
      if (ar.failed()) {
        next.handle(Future.failedFuture(ar.cause()));
        return;
      }
      UpdateResult result = ar.result();
      // Build a new whisky instance with the generated id.
      Whisky w = new Whisky(result.getKeys().getInteger(0), whisky.getName(), whisky.getOrigin());
      next.handle(Future.succeededFuture(w));
    });
  }
}
