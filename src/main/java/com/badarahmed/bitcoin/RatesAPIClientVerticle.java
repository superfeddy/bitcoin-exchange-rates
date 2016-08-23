package com.badarahmed.bitcoin;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClientOptions;

public class RatesAPIClientVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> fut) {
        EventBus eb = vertx.eventBus();
        getCurrentRates(eb);

        vertx.setPeriodic(5000, timerId -> {
            getCurrentRates(eb);
        });
    }

    private void getCurrentRates(EventBus eb) {
        vertx.createHttpClient(new HttpClientOptions().setSsl(true))
                .getNow(443, "www.okcoin.com", "/api/v1/ticker.do?symbol=btc_usd",
                        response -> {
                            if (response.statusCode() == 200) {

                                response.handler(body -> {
                                    eb.send("rates-client", body.toString(), reply -> {
                                        if (reply.succeeded()) {
                                            System.out.println("[RatesAPIClientVerticle] Received reply: " + reply.result().body());
                                        } else {
                                            System.out.println("[RatesAPIClientVerticle] No reply");
                                        }
                                    });
                                });

                            } else {
                                System.out.println("[RatesAPIClientVerticle] Bad response from API: " + response.statusCode() + " " + response.statusMessage());
                            }
                        });
    }

}
