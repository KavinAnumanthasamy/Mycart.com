package com.mycart.mycart.Processors;
import com.mycart.mycart.Exception.InventoryException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

public class FetchItemFromDBProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> dbItem = exchange.getIn().getBody(Map.class);
        if (dbItem == null) {
            throw new InventoryException("Item not found in database for update.");
        }
        exchange.setProperty("existingItem", dbItem);
    }
}
