package com.mycart.mycart.Processors;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.HashMap;
import java.util.Map;

public class InvalidPriceProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
        Map<String, String> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "BasePrice and sellingPrice must be greater than 0");
        exchange.getMessage().setBody(response);
    }
}
