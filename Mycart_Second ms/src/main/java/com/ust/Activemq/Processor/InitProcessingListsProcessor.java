package com.ust.Activemq.Processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InitProcessingListsProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        // Read the full list of inventory items from the message
        List<Map<String, Object>> itemList = exchange.getIn().getBody(List.class);

        if (itemList == null || itemList.isEmpty()) {
            throw new RuntimeException("No inventory items found in the ActiveMQ message.");
        }

        exchange.setProperty("inventoryList", itemList);
        exchange.setProperty("successList", new ArrayList<>());
        exchange.setProperty("failureList", new ArrayList<>());
    }
}
