package com.mycart.mycart.Processors;

import com.mycart.mycart.Exception.InventoryException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public class UpdateStockProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> item = exchange.getIn().getBody(Map.class);
        Map<String, Object> stockDetails = (Map<String, Object>) item.get("stockDetails");

        int availableStock = Integer.parseInt(stockDetails.get("availableStock").toString());
        int existingSoldOut = Integer.parseInt(stockDetails.get("soldOut").toString());
        int existingDamaged = Integer.parseInt(stockDetails.get("damaged").toString());

        int soldOut = exchange.getProperty("soldOut", Integer.class);
        int damaged = exchange.getProperty("damaged", Integer.class);

        if ((soldOut + damaged) > availableStock) {
            throw new InventoryException("The sum of 'soldOut' and 'damaged' exceeds the available stock for item ID : " + item.get("_id"));
        }

        int newSoldOut = existingSoldOut + soldOut;
        int newDamaged = existingDamaged + damaged;
        int newStock = availableStock - soldOut - damaged;
        if (newStock < 0) newStock = 0;

        stockDetails.put("availableStock", newStock);
        stockDetails.put("soldOut", newSoldOut);
        stockDetails.put("damaged", newDamaged);
        item.put("stockDetails", stockDetails);
        item.put("lastUpdateDate", java.time.LocalDate.now().toString());

        exchange.setProperty("updatedItem", item);
        exchange.getIn().setBody(item);
    }
}