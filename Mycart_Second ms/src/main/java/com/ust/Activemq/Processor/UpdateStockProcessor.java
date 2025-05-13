package com.ust.Activemq.Processor;

import com.ust.Activemq.Exception.InventoryException;
import com.ust.Activemq.Model.Item;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.time.LocalDate;
import java.util.Map;
public class UpdateStockProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Map<String, Object> item = exchange.getIn().getBody(Map.class);
        if (item == null) throw new InventoryException("Item not found in DB.");
        Map<String, Object> stockDetails = (Map<String, Object>) item.get("stockDetails");
        int availableStock = Integer.parseInt(stockDetails.get("availableStock").toString());
        int existingSoldOut = Integer.parseInt(stockDetails.get("soldOut").toString());
        int existingDamaged = Integer.parseInt(stockDetails.get("damaged").toString());

        int soldOut = exchange.getProperty("soldOut", Integer.class);
        int damaged = exchange.getProperty("damaged", Integer.class);

        if ((soldOut + damaged) > availableStock) {
            throw new InventoryException("Stock update exceeds available stock for item ID: " + item.get("_id"));
        }

        stockDetails.put("availableStock", availableStock - soldOut - damaged);
        stockDetails.put("soldOut", existingSoldOut + soldOut);
        stockDetails.put("damaged", existingDamaged + damaged);
        item.put("stockDetails", stockDetails);
        item.put("lastUpdateDate", LocalDate.now().toString());

        exchange.getIn().setBody(item);
    }
}

