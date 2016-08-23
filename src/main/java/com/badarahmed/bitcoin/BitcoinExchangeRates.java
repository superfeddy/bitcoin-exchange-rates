package com.badarahmed.bitcoin;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

public class BitcoinExchangeRates extends AbstractVerticle {

    String bufferRawRate;

    @Override
    public void start(Future<Void> fut) {
        vertx.deployVerticle(RatesAPIClientVerticle.class.getName());

        EventBus eb = vertx.eventBus();
        eb.consumer("rates-client", message -> {
            System.out.println("[BitcoinExchangeRates] Received latest rate: " + message.body());
            message.reply("ack");
            bufferRawRate = message.body().toString();
        });

        vertx
                .createHttpServer()
                .requestHandler(r -> {
                    r.response().end("<h1>Hello from my first " +
                            "Vert.x 3 application</h1>");
                })
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }
}