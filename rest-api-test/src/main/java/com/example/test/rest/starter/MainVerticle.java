package com.example.test.rest.starter;

import java.util.HashSet;
import java.util.Set;

import com.test.ProductService;
import com.test.entities.Product;
import com.test.verticles.BaseVerticle;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class MainVerticle extends BaseVerticle {

  EventBus eb;

  protected ServiceDiscovery discovery;

  public static final String JDBC_ADDRESS = "jdbc-service";

  ProductService service;

  @Override
  public void start(Future<Void> future) throws Exception {

    super.start(future);

    super.initCluster().setHandler(r -> {

      if (r.succeeded()) {

        init(future);
      }
    });

  }

  public void init(Future<Void> future) {

    ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress("jdbc-service");

    service = builder.build(ProductService.class);

    eb = vertx.eventBus();

    Router router = Router.router(vertx);
    // CORS support
    Set<String> allowHeaders = new HashSet<>();
    allowHeaders.add("x-requested-with");
    allowHeaders.add("Access-Control-Allow-Origin");
    allowHeaders.add("Access-Control-Allow-Headers");
    allowHeaders.add("Access-Control-Request-Method");
    allowHeaders.add("Access-Control-Allow-Credentials");

    allowHeaders.add("origin");
    allowHeaders.add("Content-Type");
    allowHeaders.add("accept");
    Set<HttpMethod> allowMethods = new HashSet<>();
    allowMethods.add(HttpMethod.GET);
    allowMethods.add(HttpMethod.POST);
    allowMethods.add(HttpMethod.PUT);
    allowMethods.add(HttpMethod.DELETE);
    allowMethods.add(HttpMethod.PATCH);

    router.route().handler(CorsHandler.create("*").allowedHeaders(allowHeaders).allowedMethods(allowMethods));

    router.route().handler(BodyHandler.create());

    //router.get("/products").handler(this::initData);
    router.post("/products").handler(this::addProduct);
    router.get("/products").handler(this::list);
    router.put("/products/:id").handler(this::updateProduct);
    router.delete("/products/:id").handler(this::deleteProduct);


    // router.get("/insert").handler(this::insertData);

    // router.get("/list").handler(this::listData);

    // routes

    vertx.createHttpServer().requestHandler(router::accept).listen(8080, "localhost", result -> {
      if (result.succeeded())
        future.complete();
      else
        future.fail(result.cause());
    });

    HealthCheckHandler healthCheckHandler1 = HealthCheckHandler.create(vertx);
    HealthCheckHandler healthCheckHandler2 = HealthCheckHandler.createWithHealthChecks(HealthChecks.create(vertx));

    // Populate the router with routes...
    // Register the health check handler
    router.get("/health*").handler(healthCheckHandler1);
    // Or
    router.get("/ping*").handler(healthCheckHandler2);
  }

  private void initData(RoutingContext context) {

    service.initializePersistence((r) -> {
      System.out.println("INIT SEND REPLY INITIALIZE");
      if (r.succeeded()) {

        context.response().putHeader("content-type", "application/json; charset=utf-8");
        context.response().end("{\"result\":\"OK\"}");
      } else {
        r.cause().printStackTrace();
        context.response().end(r.cause().getMessage());
      }

    });

    /*
     * eb.send(JDBC_ADDRESS, "give me some", reply -> {
     * System.out.println("this is reply"); System.out.println(reply.result());
     * 
     * context.response().end(reply.result().body().toString()); });
     */
  }

  private void addProduct(RoutingContext context) {
    System.out.println("ADD PRODUCT REST");
    Product p = new Product(context.getBodyAsJson());
   
    service.addProduct(p, (r) -> {
      System.out.println("INIT SEND REPLY");
      System.out.println(r);
      if (r.succeeded()) {

        context.response().putHeader("content-type", "application/json; charset=utf-8");
        context.response().end("{\"result\":\"OK\"}");
      } else {
        r.cause().printStackTrace();
        context.response().end(r.cause().getMessage());
      }

    });
  }

  private void updateProduct(RoutingContext context) {
    System.out.println("ADD PRODUCT REST");
    Product p = new Product(context.getBodyAsJson());
    p.setProductId(context.request().getParam("id"));

    service.updateProduct(p, (r) -> {
      System.out.println("INIT SEND REPLY");
      System.out.println(r);
      if (r.succeeded()) {

        context.response().putHeader("content-type", "application/json; charset=utf-8");
        context.response().end("{\"result\":\"OK\"}");
      } else {
        r.cause().printStackTrace();
        context.response().end(r.cause().getMessage());
      }

    });
  }

  private void deleteProduct(RoutingContext context) {
    System.out.println("ADD PRODUCT REST");
    String productId = context.request().getParam("id");

    service.deleteProduct(productId, (r) -> {
      System.out.println("INIT SEND REPLY");
      System.out.println(r);
      if (r.succeeded()) {

        context.response().putHeader("content-type", "application/json; charset=utf-8");
        context.response().end("{\"result\":\"OK\"}");
      } else {
        r.cause().printStackTrace();
        context.response().end(r.cause().getMessage());
      }

    });
  }

  private void list(RoutingContext context) {

    service.retrieveAllProducts((r) -> {
      System.out.println("INIT SEND REPLY LIST");
      System.out.println(r);
      if (r.succeeded()) {
        context.response().putHeader("content-type", "application/json; charset=utf-8");
        context.response().end(r.result().toString());
      } else {
        r.cause().printStackTrace();
        context.response().end(r.cause().getMessage());
      }

    });
  }

  /*
   * private void insertData(RoutingContext context) {
   * 
   * int randomId = (int) (Math.random() * 100); Todo item = new Todo(randomId,
   * "test", Boolean.TRUE, 1, "url");
   * 
   * service.insert(item);
   * 
   * context.response().end("OK");
   * 
   * }
   * 
   * private void listData(RoutingContext context) {
   * System.out.println("list data");
   * service.getAll().setHandler(resultHandler(context, res -> {
   * System.out.println("handler"); System.out.println(res); if (res == null) { //
   * serviceUnavailable(context); } else { final String encoded =
   * Json.encodePrettily(res); context.response().putHeader("content-type",
   * "application/json").end(encoded); } }));
   * 
   * }
   * 
   * private <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context,
   * Consumer<T> consumer) { return res -> { if (res.succeeded()) {
   * consumer.accept(res.result()); } else { // serviceUnavailable(context); } };
   * }
   */
}
