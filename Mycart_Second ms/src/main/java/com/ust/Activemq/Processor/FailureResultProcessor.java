package com.ust.Activemq.Processor;

import com.ust.Activemq.Model.Item;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FailureResultProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        String itemId = exchange.getProperty("itemId", String.class);
        String errorMsg = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage();
        List<Map<String, Object>> failureList = exchange.getProperty("failureList", List.class);

        Map<String, Object> result = new HashMap<>();
        result.put("itemId", itemId);
        result.put("status", "failure");
        result.put("error", errorMsg);
        failureList.add(result);

        // Log the error
        exchange.getContext().createProducerTemplate().sendBody("log:Error", "Failed to update item " + itemId + " Error: " + errorMsg);
    }
}