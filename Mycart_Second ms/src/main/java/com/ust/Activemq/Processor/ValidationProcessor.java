package com.ust.Activemq.Processor;

import com.ust.Activemq.Exception.InventoryException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public class ValidationProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Map<String, Object> item = exchange.getIn().getBody(Map.class);
        if (item == null || item.get("_id") == null || item.get("stockDetails") == null) {
            throw new InventoryException("Item ID or stock details are missing.");
        }
        // Convert _id to string safely (handle ObjectId, Integer, String)
        Object idObj = item.get("_id");
        String id = idObj != null ? idObj.toString() : null;
        if (id == null || id.isEmpty()) {
            throw new InventoryException("Invalid item ID.");
        }

        Map<String, Object> stock = (Map<String, Object>) item.get("stockDetails");
        if (stock.get("soldOut") == null || stock.get("damaged") == null) {
            throw new InventoryException("soldOut or damaged fields are missing in stockDetails.");
        }

        int soldOut;
        int damaged;
        try {
            soldOut = Integer.parseInt(stock.get("soldOut").toString());
            damaged = Integer.parseInt(stock.get("damaged").toString());
        } catch (NumberFormatException e) {
            throw new InventoryException("soldOut or damaged must be valid integers.");
        }

        exchange.setProperty("itemId", id);
        exchange.setProperty("soldOut", soldOut);
        exchange.setProperty("damaged", damaged);
    }
}
