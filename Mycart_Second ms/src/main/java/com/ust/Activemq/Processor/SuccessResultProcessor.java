package com.ust.Activemq.Processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuccessResultProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        String itemId = exchange.getProperty("itemId", String.class);
        List<Map<String, Object>> successList = exchange.getProperty("successList", List.class);

        Map<String, Object> result = new HashMap<>();
        result.put("itemId", itemId);
        result.put("status", "success");
        result.put("message", "Inventory updated successfully for item " + itemId);
        successList.add(result);

        exchange.getContext().createProducerTemplate().sendBody("log:SuccessResult", "Item updated: " + itemId);
    }
}