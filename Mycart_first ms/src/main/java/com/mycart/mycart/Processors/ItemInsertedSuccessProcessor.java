package com.mycart.mycart.Processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.HashMap;
import java.util.Map;

public class ItemInsertedSuccessProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);
        Map<String,String> response = new HashMap<>();
        response.put("stauts","Success");
        response.put("message", "Item inserted successfully");
        exchange.getMessage().setBody(response);
    }
}
