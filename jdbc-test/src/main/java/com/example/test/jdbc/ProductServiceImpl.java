package com.example.test.jdbc;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.test.ProductService;
import com.test.entities.Product;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * JDBC implementation of {@link io.vertx.blueprint.microservice.product.ProductService}.
 *
 * @author Eric Zhao
 */
public class ProductServiceImpl extends JdbcRepositoryWrapper implements ProductService {

  private static final int PAGE_LIMIT = 10;

  Vertx vertx;
  EventBus eb;


  public ProductServiceImpl(Vertx vertx, JsonObject config) {
    super(vertx, config);

    this.vertx = vertx;

    this.eb = vertx.eventBus();
  }



@Override
  public ProductService initializePersistence(Handler<AsyncResult<Void>> resultHandler) {
    System.out.println("initialize-----");
    client.getConnection(connHandler(resultHandler, connection -> {
      connection.execute(CREATE_STATEMENT, r -> {
        resultHandler.handle(r);
        connection.close();
      });
    }));
    return this;
  }

  @Override
  public ProductService addProduct(Product product, Handler<AsyncResult<Void>> resultHandler) {

    System.out.println("this is add product");

   // String productId = product.getProductId();
    product.setProductId(UUID.randomUUID().toString());

    JsonArray params = new JsonArray()
      .add(product.getProductId())
      .add(product.getSellerId())
      .add(product.getName())
      .add(product.getPrice())
      .add(product.getIllustration())
      .add(product.getType());
      System.out.println(product);
      System.out.println(params);

      System.out.println("PUBLISH BEFORE");
  
    
    executeNoResult(params, INSERT_STATEMENT, resultHandler);
    eb.publish("product-created", product.toJson());
    System.out.println("PUBLISH AFTER");
    resultHandler.handle(Future.succeededFuture());
    return this;
  }

  @Override
  public ProductService updateProduct(Product product, Handler<AsyncResult<Void>> resultHandler) {

    System.out.println("this is update product");
    JsonArray params = new JsonArray()
      .add(product.getSellerId())
      .add(product.getName())
      .add(product.getPrice())
      .add(product.getIllustration())
      .add(product.getType())
      .add(product.getProductId());
      System.out.println(product);
      System.out.println(params);

      System.out.println("PUBLISH BEFORE");
  
    
    executeNoResult(params, UPDATE_STATEMENT, resultHandler);
    eb.publish("product-updated", product.toJson());
    System.out.println("PUBLISH AFTER");
    resultHandler.handle(Future.succeededFuture());
    return this;
  }

  @Override
  public ProductService retrieveProduct(String productId, Handler<AsyncResult<Product>> resultHandler) {
    this.retrieveOne(productId, FETCH_STATEMENT)
      .map(option -> option.map(Product::new).orElse(null))
      .setHandler(resultHandler);
    return this;
  }

  @Override
  public ProductService retrieveProductPrice(String productId, Handler<AsyncResult<JsonObject>> resultHandler) {
    this.retrieveOne(productId, "SELECT price FROM product WHERE productId = ?")
      .map(option -> option.orElse(null))
      .setHandler(resultHandler);
    return this;
  }

  @Override
  public ProductService retrieveProductsByPage(int page, Handler<AsyncResult<List<Product>>> resultHandler) {
    this.retrieveByPage(page, PAGE_LIMIT, FETCH_WITH_PAGE_STATEMENT)
      .map(rawList -> rawList.stream()
        .map(Product::new)
        .collect(Collectors.toList())
      )
      .setHandler(resultHandler);
    return this;
  }

  @Override
  public ProductService retrieveAllProducts(Handler<AsyncResult<List<Product>>> resultHandler) {
    this.retrieveAll(FETCH_ALL_STATEMENT)
      .map(rawList -> rawList.stream()
        .map(Product::new)
        .collect(Collectors.toList())
      )
      .setHandler(resultHandler);
    return this;
  }

  @Override
  public ProductService deleteProduct(String productId, Handler<AsyncResult<Void>> resultHandler) {
    this.removeOne(productId, DELETE_STATEMENT, resultHandler);
    Product p = new Product();
    p.setProductId(productId);
    eb.publish("product-deleted", p.toJson()) ;
    return this;
  }

  @Override
  public ProductService deleteAllProducts(Handler<AsyncResult<Void>> resultHandler) {
    this.removeAll(DELETE_ALL_STATEMENT, resultHandler);
    return this;
  }

  // SQL statements

  private static final String CREATE_STATEMENT = "CREATE TABLE IF NOT EXISTS product (\n" +
    "  productId VARCHAR(80) NOT NULL,\n" +
    "  sellerId varchar(30)  NULL,\n" +
    "  name varchar(255)  NULL,\n" +
    "  price double  NULL,\n" +
    "  illustration VARCHAR(60)  NULL,\n" +
    "  type varchar(45)  NULL,\n" +
    "  PRIMARY KEY (productId),\n" +
    "  )";
  private static final String INSERT_STATEMENT = "INSERT INTO product (productId, sellerId, name, price, illustration, type) VALUES (?, ?, ?, ?, ?, ?)";
  private static final String FETCH_STATEMENT = "SELECT * FROM product WHERE productId = ?";
  private static final String FETCH_ALL_STATEMENT = "SELECT * FROM product";
  private static final String FETCH_WITH_PAGE_STATEMENT = "SELECT * FROM product LIMIT ?, ?";
  private static final String DELETE_STATEMENT = "DELETE FROM product WHERE productId = ?";
  private static final String DELETE_ALL_STATEMENT = "DELETE FROM product";
  private static final String UPDATE_STATEMENT = "UPDATE  product set sellerId = ?, name = ?, price = ?, illustration = ?, type = ? where productId = ?";

  @Override
  public void close() {
     // this.close();
  }
}