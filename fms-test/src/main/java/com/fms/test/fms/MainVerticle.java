package com.fms.test.fms;

import com.test.verticles.BaseVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

public class MainVerticle extends BaseVerticle {

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

  public void init(){

   EventBus eb = vertx.eventBus();

    eb.consumer("product-cretaed", r -> {

      System.out.println("this is rms");
      System.out.println(r.body());
    });
  }

}
