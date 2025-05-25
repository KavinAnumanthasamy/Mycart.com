package com.mycart.mycart.Processors;

import com.mycart.mycart.Exception.InventoryException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FlattenInventoryProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> body = exchange.getIn().getBody(Map.class);
        Object rawItems = body.get("items");
        List<Map<String, Object>> flatItemList = new ArrayList<>();
        if (rawItems instanceof List<?>) {
            for (Object group : (List<?>) rawItems) {
                if (group instanceof List<?>) {
                    for (Object item : (List<?>) group) {
                        if (item instanceof Map) {
                            flatItemList.add((Map<String, Object>) item);
                        }
                    }
                } else if (group instanceof Map) {
                    flatItemList.add((Map<String, Object>) group);
                }
            }
        }
        if (flatItemList.isEmpty()) {
            throw new InventoryException("No valid inventory items found.");
        }

        exchange.setProperty("inventoryList", flatItemList);
    }
}