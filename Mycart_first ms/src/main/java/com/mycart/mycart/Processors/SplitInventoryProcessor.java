package com.mycart.mycart.Processors;

import com.mycart.mycart.Exception.InventoryException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.List;

public class SplitInventoryProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Object itemsProp = exchange.getProperty("inventoryList");
//        Is itemsProp a List?              Is the list empty?
        if (!(itemsProp instanceof List) || ((List<?>) itemsProp).isEmpty()) {
            throw new InventoryException("inventory list is missing or not a valid list");
        }
    }
}
