//package com.mycart.mycart.Processors;
//
//import org.apache.camel.Exchange;
//import org.apache.camel.Processor;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class ItemsResponseProcessor implements Processor {
//    @Override
//    public void process(Exchange exchange) throws Exception {
//        List<?> body = exchange.getIn().getBody(List.class);
//        if (body == null || body.isEmpty()) {
//            exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
//            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
//            Map<String, String> errorResp = new HashMap<>();
//            errorResp.put("status", "error");
//            errorResp.put("message", "No items found for the given category.");
//            exchange.getMessage().setBody(errorResp);
//        } else {
//            exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
//            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
//            exchange.getMessage().setBody(body.get(0));
//        }
//    }
//}
package com.mycart.mycart.Processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemsResponseProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        List<Document> body = exchange.getIn().getBody(List.class);

        if (body == null || body.isEmpty()) {
            exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
            Map<String, String> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", "No items found for the given category.");
            exchange.getMessage().setBody(errorResp);
            return;
        }

        Document resultDoc = body.get(0); // This contains category info and items

        String categoryName = resultDoc.getString("categoryName");
        String categoryDepartment = resultDoc.getString("categoryDepartment");
        List<?> items = (List<?>) resultDoc.get("items");

        // Replace each item with an empty object
        List<Map<String, Object>> emptyItems = new ArrayList<>();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                emptyItems.add(new HashMap<>());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("categoryName", categoryName);
        response.put("categoryDepartment", categoryDepartment);
        response.put("items", items);

        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        exchange.getMessage().setBody(response);
    }
}
