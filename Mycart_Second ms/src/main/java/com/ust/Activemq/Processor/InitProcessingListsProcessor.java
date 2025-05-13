package com.ust.Activemq.Processor;

import com.ust.Activemq.Exception.InventoryException;
import com.ust.Activemq.Model.Item;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InitProcessingListsProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        Map<String, Object> item = exchange.getIn().getBody(Map.class);
        List<Map<String, Object>> itemList = new ArrayList<>();
        itemList.add(item);
        exchange.setProperty("inventoryList", itemList);
        exchange.setProperty("successList", new ArrayList<>());
        exchange.setProperty("failureList", new ArrayList<>());
    }
}