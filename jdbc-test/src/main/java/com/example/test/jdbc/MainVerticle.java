package com.example.test.jdbc;

import java.util.function.Consumer;

import com.test.ProductService;
import com.test.entities.Product;
import com.test.entities.Todo;
import com.test.verticles.BaseVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.HealthChecks;
import io.vertx.ext.web.Router;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class MainVerticle extends BaseVerticle {

  private TodoService service;


  public static final String ADDRESS = "jdbc-service";

  @Override
  public void start(Future<Void> future) throws Exception {

    super.start(future);

    super.initCluster().setHandler(r -> {

      System.out.println(r.succeeded());

      if(r.succeeded()){

        init();
      }
    });


  }

  public void init() {

    HealthCheckHandler healthCheckHandler1 = HealthCheckHandler.create(vertx);
    HealthCheckHandler healthCheckHandler2 = HealthCheckHandler.createWithHealthChecks(HealthChecks.create(vertx));

    Router router = Router.router(vertx);
    // Populate the router with routes...
    // Register the health check handler
    router.get("/health*").handler(healthCheckHandler1);
    // Or
    router.get("/ping*").handler(healthCheckHandler2);

    ProductService s = new ProductServiceImpl(vertx, config());
    // Register the handler
    new ServiceBinder(vertx).setAddress("jdbc-service").register(ProductService.class, s);

    EventBus eb = vertx.eventBus();
    /*
     * eb.consumer(ADDRESS, message -> {
     * 
     * 
     * System.out.println(ADDRESS + " / " + message.body());
     * 
     * message.reply("this is from jdbc test service");
     * 
     * 
     * 
     * });
     * 
     */

     s.initializePersistence(r -> {

     });

    service = new JdbcTodoService(vertx, config());

    // initData();

  }

  private void addProduct(ProductService s) {

    Product p = new Product();
    p.setName("sss");
    p.setProductId(((int) (Math.random() * 100)) + "");

    s.addProduct(p, r -> {

      System.out.println(r.succeeded());
      if (!r.succeeded()) {

        r.cause().printStackTrace();

        s.retrieveAllProducts(rr -> {
          System.out.println(rr.succeeded());
          System.out.println(rr.result());
        });
      }

      s.retrieveAllProducts(rr -> {

        System.out.println(rr.result());
      });
    });
  }

  private void initData() {

    // service.initData();

    // context.response().end("OK");
  }

  private void insertData() {

    int randomId = (int) (Math.random() * 100);
    Todo item = new Todo(randomId, "test", Boolean.TRUE, 1, "url");

    service.insert(item);

    // context.response().end("OK");

  }

  private Future<JsonObject> listData() {
    Future<JsonObject> future = Future.future();

    System.out.println("list data");
    service.getAll().setHandler(resultHandler(res -> {
      System.out.println("handler");
      System.out.println(res);
      if (res == null) {
        // serviceUnavailable(context);
      } else {
        future.complete(new JsonObject().put("pages", new JsonArray(res)));
      }
    }));

    return future;
  }

  private <T> Handler<AsyncResult<T>> resultHandler(Consumer<T> consumer) {
    return res -> {
      if (res.succeeded()) {
        consumer.accept(res.result());
      } else {
        // serviceUnavailable(context);
      }
    };
  }

}