package com.mycart.mycart.Processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetAllCategoryProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        List<?> categories = exchange.getIn().getBody(List.class);
        if (categories == null || categories.isEmpty()) {
            exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
            Map<String, String> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", "No categories found in the database.");
            exchange.getMessage().setBody(errorResp);
        } else {
            exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
            // Body already contains the list of categories, no change needed
        }
    }
}
