package com.mycart.mycart.Processors;

import com.mycart.mycart.Exception.InventoryException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public class UpdateStockProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        // Get existing DB item from exchange property (set in earlier processor)
        Map<String, Object> existingItem = exchange.getProperty("existingItem", Map.class);
        if (existingItem == null) {
            throw new InventoryException("Item not found in DB for update");
        }

        // Get existing stockDetails map
        Map<String, Object> stockDetails = (Map<String, Object>) existingItem.get("stockDetails");
        if (stockDetails == null) {
            throw new InventoryException("DB item missing stockDetails");
        }

        // Parse existing values
        int availableStock = stockDetails.get("availableStock") != null ? Integer.parseInt(stockDetails.get("availableStock").toString()) : 0;
        int existingSoldOut = stockDetails.get("soldOut") != null ? Integer.parseInt(stockDetails.get("soldOut").toString()) : 0;
        int existingDamaged = stockDetails.get("damaged") != null ? Integer.parseInt(stockDetails.get("damaged").toString()) : 0;

        // Get incoming soldOut and damaged from exchange properties (set earlier in route)
        Integer soldOut = exchange.getProperty("soldOut", Integer.class);
        Integer damaged = exchange.getProperty("damaged", Integer.class);
        if (soldOut == null) soldOut = 0;
        if (damaged == null) damaged = 0;

        // Validation: sum of soldOut + damaged cannot exceed availableStock
        if ((soldOut + damaged) > availableStock) {
            throw new InventoryException("The sum of 'soldOut' and 'damaged' exceeds the available stock for item ID : " + existingItem.get("_id"));
        }

        // Calculate new values
        int newSoldOut = existingSoldOut + soldOut;
        int newDamaged = existingDamaged + damaged;
        int newAvailableStock = availableStock - soldOut - damaged;
        if (newAvailableStock < 0) newAvailableStock = 0;

        // Update stockDetails
        stockDetails.put("availableStock", newAvailableStock);
        stockDetails.put("soldOut", newSoldOut);
        stockDetails.put("damaged", newDamaged);

        // Update timestamp
        existingItem.put("lastUpdateDate", java.time.LocalDate.now().toString());
        existingItem.put("stockDetails", stockDetails);

        // Set updated item in exchange for next step (e.g., mongo save)
        exchange.setProperty("updatedItem", existingItem);
        exchange.getIn().setBody(existingItem);
    }
}
