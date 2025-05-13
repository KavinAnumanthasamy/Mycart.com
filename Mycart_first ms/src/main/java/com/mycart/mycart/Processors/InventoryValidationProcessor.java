package com.mycart.mycart.Processors;

import com.mycart.mycart.Exception.InventoryException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.List;
import java.util.Map;

public class InventoryValidationProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> body = exchange.getIn().getBody(Map.class);

        // Validate top-level 'items' field
        if (body == null || !body.containsKey("items") || body.get("items") == null) {
            throw new InventoryException("'items' field is missing or empty.");
        }

        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) body.get("items");
        if (itemsList.isEmpty()) {
            throw new InventoryException("'items' list is empty.");
        }

        // Validate each item inside 'items'
        for (Map<String, Object> item : itemsList) {
            if (item == null) {
                throw new InventoryException("Item is null inside 'items' array.");
            }
            if (item.get("_id") == null) {
                throw new InventoryException("Item ID ('_id') is missing for an item.");
            }
            if (item.get("stockDetails") == null) {
                throw new InventoryException("Stock details ('stockDetails') are missing for item ID: " + item.get("_id"));
            }

            Map<String, Object> stock = (Map<String, Object>) item.get("stockDetails");

            Object soldOutObj = stock.get("soldOut");
            Object damagedObj = stock.get("damaged");

            if (soldOutObj == null || damagedObj == null) {
                throw new InventoryException("'soldOut' or 'damaged' fields missing for item ID: " + item.get("_id"));
            }

            try {
                Integer.parseInt(soldOutObj.toString());
                Integer.parseInt(damagedObj.toString());
            } catch (NumberFormatException e) {
                throw new InventoryException("'soldOut' or 'damaged' is not a valid number for item ID: " + item.get("_id"));
            }
        }
    }
}
