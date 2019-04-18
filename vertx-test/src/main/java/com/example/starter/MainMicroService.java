package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

public class MainMicroService extends AbstractVerticle {

    @Override
    public void start() {

        vertx.eventBus().consumer("hello", message -> {
            System.out.println("I have received a message: " + message.body());
          }).completionHandler(res -> {
            if (res.succeeded()) {
              System.out.println("The handler registration has reached all nodes");
            } else {
              System.out.println("Registration failed!");
            }
          });
/*
          DeliveryOptions options = new DeliveryOptions();
options.addHeader("some-header", "some-value");
vertx.eventBus().send("hello", "Yay! Someone kicked a ball", options);

*/
    }
}
