package com.mycart.mycart.Processors;

import com.mycart.mycart.Exception.InventoryException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public class FetchItemFromDBProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> item = exchange.getIn().getBody(Map.class);
        if (item == null) {
            throw new InventoryException("Item not found in DB for update.");
        }

        Map<String, Object> stockDetails = (Map<String, Object>) item.get("stockDetails");
        if (stockDetails == null) {
            throw new InventoryException("Stock details missing in DB for item: " + item.get("_id"));
        }
    }
}