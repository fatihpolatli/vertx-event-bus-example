package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class Deployer extends AbstractVerticle {

  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(new MainVerticle());
    vertx.deployVerticle(new MainMicroService());
  }

}
