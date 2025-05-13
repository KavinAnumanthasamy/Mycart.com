package com.ust.Activemq.Processor;

import com.ust.Activemq.Exception.InventoryException;
import com.ust.Activemq.Model.Item;
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
        String id = item.get("_id").toString();
        Map<String, Object> stock = (Map<String, Object>) item.get("stockDetails");
        int soldOut = Integer.parseInt(stock.get("soldOut").toString());
        int damaged = Integer.parseInt(stock.get("damaged").toString());
        exchange.setProperty("itemId", id);
        exchange.setProperty("soldOut", soldOut);
        exchange.setProperty("damaged", damaged);
    }
}
