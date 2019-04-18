package com.test.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

public class BaseVerticle extends AbstractVerticle{

    public Vertx vertx;

    @Override
    public void start(Future<Void> future) throws Exception {
  
      
  
    } 

    public Future<Vertx> initCluster(){

        Future<Vertx> future = Future.future();

        ClusterManager mgr = new HazelcastClusterManager();
  
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
    
        Vertx.clusteredVertx(options, res -> {
          if (res.succeeded()) {
            vertx = res.result();
            
            future.complete(vertx);
            
          } else {
            // failed!
    
            System.out.println("FAILED");
          }
        });

        return future;
    }

}