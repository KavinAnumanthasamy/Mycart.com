package com.mycart.mycart.Processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.HashMap;
import java.util.Map;

public class ItemAlreadyExistsProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Map<?, ?> item = exchange.getProperty("item", Map.class);
        String itemId = (item != null && item.get("_id") != null) ? item.get("_id").toString() : "unknown";

        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);

        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Item with _id '" + itemId + "' already exists");
        exchange.getMessage().setBody(response);
    }
}
