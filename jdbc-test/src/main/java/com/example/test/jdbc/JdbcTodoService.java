package com.example.test.jdbc;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.test.entities.Todo;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class JdbcTodoService implements TodoService {

    private final Vertx vertx;
    private final JsonObject config;
    private final JDBCClient client;

    public JdbcTodoService(JsonObject config) {
        this(Vertx.vertx(), config);
    }

    public JdbcTodoService(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;

        config.put("url", "jdbc:hsqldb:mem:db?shutdown=true");
        config.put("driver_class", "org.hsqldb.jdbcDriver");
       
     
        this.client = JDBCClient.createShared(vertx, config, "My-Todo-Collection");
    }

    private Handler<AsyncResult<SQLConnection>> connHandler(Future future, Handler<SQLConnection> handler) {
        return conn -> {
            if (conn.succeeded()) {
                final SQLConnection connection = conn.result();
                handler.handle(connection);
            } else {
                future.fail(conn.cause());
            }
        };
    }

    @Override
    public Future<Boolean> initData() {

        Future<Boolean> result = Future.future();
        client.getConnection(connHandler(result, connection -> connection.execute(SQL_CREATE, create -> {
            if (create.succeeded()) {
                result.complete(true);
            } else {
                result.fail(create.cause());
            }
            connection.close();
        })));
        return result;

    }

    @Override
    public Future<Boolean> insert(Todo todo) {
        Future<Boolean> result = Future.future();
        client.getConnection(connHandler(result, connection -> {
            connection.updateWithParams(SQL_INSERT, new JsonArray().add(todo.getUrl()).add(todo.getTitle())
                    .add(todo.isCompleted()).add(todo.getOrder()).add(todo.getUrl()), r -> {
                        if (r.failed()) {
                            result.fail(r.cause());
                        } else {
                            result.complete(true);
                        }
                        connection.close();
                    });
        }));
        return result;
    }

    @Override
    public Future<Optional<Todo>> getCertain(String todoID) {

        Future<Optional<Todo>> result = Future.future();
        client.getConnection(connHandler(result, connection -> {
            connection.queryWithParams(SQL_QUERY, new JsonArray().add(todoID), r -> {
                if (r.failed()) {
                    result.fail(r.cause());
                } else {
                    List<JsonObject> list = r.result().getRows();
                    if (list == null || list.isEmpty()) {
                        result.complete(Optional.empty());
                    } else {
                        result.complete(Optional.of(new Todo(list.get(0))));
                    }
                }
                connection.close();
            });
        }));
        return result;
    }

    private static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS `todo` (\n"
            + "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" + "  `title` varchar(255) DEFAULT NULL,\n"
            + "  `completed` tinyint(1) DEFAULT NULL,\n" + "  `order` int(11) DEFAULT NULL,\n"
            + "  `url` varchar(255) DEFAULT NULL,\n" + "  PRIMARY KEY (`id`) )";
    private static final String SQL_INSERT = "INSERT INTO `todo` "
            + "(`id`, `title`, `completed`, `order`, `url`) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_QUERY = "SELECT * FROM todo WHERE id = ?";
    private static final String SQL_QUERY_ALL = "SELECT * FROM todo";
    private static final String SQL_UPDATE = "UPDATE `todo`\n" + "SET `id` = ?,\n" + "`title` = ?,\n"
            + "`completed` = ?,\n" + "`order` = ?,\n" + "`url` = ?\n" + "WHERE `id` = ?;";
    private static final String SQL_DELETE = "DELETE FROM `todo` WHERE `id` = ?";
    private static final String SQL_DELETE_ALL = "DELETE FROM `todo`";

    @Override
    public Future<List<Todo>> getAll() {

        Future<List<Todo>> result = Future.future();

        client.getConnection(connHandler(result, connection -> {
            connection.query(SQL_QUERY_ALL, res -> {
                if (res.succeeded()) {
                   
                    List<Todo> pages = res.result().getResults().stream().map(json -> new Todo(json.getJsonObject(0)))
                            .collect(Collectors.toList());
                    result.complete(pages);
                } else {
                    res.cause().printStackTrace();
                    System.out.println("ERROR");
                }
            });
        }));

        return result;
    }

    @Override
    public Future<Todo> update(String todoId, Todo newTodo) {
        return null;
    }

    @Override
    public Future<Boolean> delete(String todoId) {
        return null;
    }

    @Override
    public Future<Boolean> deleteAll() {
        return null;
    }
}