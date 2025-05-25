package com.mycart.mycart.Processors;

import com.mycart.mycart.Exception.InventoryException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.List;
import java.util.Map;

public class AMQExtractItemsArrayProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> requestBody = exchange.getIn().getBody(Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) requestBody.get("items");
        if (items == null) {
            throw new InventoryException("Missing 'items' field in the input");
        }
        exchange.getIn().setBody(items); // Send full list to ActiveMQ
    }
}
