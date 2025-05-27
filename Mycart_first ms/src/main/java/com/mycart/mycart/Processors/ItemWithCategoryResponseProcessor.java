package com.mycart.mycart.Processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemWithCategoryResponseProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, Object> item = exchange.getProperty("itemData", Map.class);
        Map<String, Object> category = exchange.getIn().getBody(Map.class);

        String categoryName = category != null ? (String) category.get("categoryName") : null;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_id", item.get("_id"));
        response.put("itemName", item.get("itemName"));
        response.put("categoryName", categoryName);
        response.put("itemPrice", item.get("itemPrice"));
        response.put("stockDetails", item.get("stockDetails"));
        response.put("specialProduct", item.get("specialProduct"));

        exchange.getMessage().setBody(response);
    }
}
