package com.mycart.mycart.Processors;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.HashMap;
import java.util.Map;

public class CategoryNotFoundProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Map<?, ?> item = exchange.getProperty("item", Map.class);
        String categoryId = (item != null && item.get("categoryId") != null) ? item.get("categoryId").toString() : "unknown";

        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
        Map<String, String>response =new HashMap<>();
        response.put("status", "error");
        response.put("message", "Category id does not exist");
        exchange.getMessage().setBody(response);
    }
}
