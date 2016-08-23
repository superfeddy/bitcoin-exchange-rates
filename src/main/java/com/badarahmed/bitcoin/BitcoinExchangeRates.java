package com.badarahmed.bitcoin;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

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

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/rates/average").handler(this::handleGetAvg);
        router.get("/rates/historical/:time").handler(this::handleGetHistoricalAvg);
        router.get("/rates/historical/:time/:exchange").handler(this::handleGetRawResp);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void handleGetAvg(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        JsonObject currentRates = convertRates(new JsonObject(bufferRawRate));

        response.putHeader("content-type", "application/json").end(currentRates.encodePrettily());
    }

    private JsonObject convertRates(JsonObject raw) {
        JsonObject ticker = raw.getJsonObject("ticker");

        float bid = Float.parseFloat(ticker.getString("sell"));
        float ask = Float.parseFloat(ticker.getString("buy"));
        float last = Float.parseFloat(ticker.getString("last"));

        return new JsonObject()
                .put("bid", bid)
                .put("ask", ask)
                .put("last", last);
    }

    private void handleGetHistoricalAvg(RoutingContext routingContext) {

    }

    private void handleGetRawResp(RoutingContext routingContext) {

    }

}