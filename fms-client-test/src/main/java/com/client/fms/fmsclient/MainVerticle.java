package com.client.fms.fmsclient;

import com.test.ProductService;
import com.test.verticles.BaseVerticle;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class MainVerticle extends BaseVerticle {

  ProductService service;

  @Override
  public void start(Future<Void> future) throws Exception {

    super.start(future);

    super.initCluster().setHandler(r -> {

      if (r.succeeded()) {

        init();
      }

    });

  }

  public void init() {


    ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx).setAddress("jdbc-service");

    service = builder.build(ProductService.class);
    
    Router router = Router.router(vertx);

    router.route().handler(staticHandler());
    // Allow events for the designated addresses in/out of the event bus bridge
    BridgeOptions opts = new BridgeOptions()
        .addOutboundPermitted(new PermittedOptions().setAddress("product-created"))
        .addOutboundPermitted(new PermittedOptions().setAddress("product-created-client"))
        .addOutboundPermitted(new PermittedOptions().setAddress("product-updated"))
        .addOutboundPermitted(new PermittedOptions().setAddress("product-deleted"));

    // Create the event bus bridge and add it to the router.
    SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
    router.route("/eventbus/*").handler(ebHandler);



    vertx.createHttpServer().requestHandler(router::accept).listen(8081);

  
    

    
  }

  private StaticHandler staticHandler() {
    return StaticHandler.create().setCachingEnabled(false);
  }

}
