package com.ust.Activemq.Processor;

import com.ust.Activemq.Model.ResultDocument;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.time.LocalDateTime;
import java.util.*;

public class FinalizeResultDocumentProcessor implements Processor {
    @Override
    public void process(Exchange exchange) {
        String requestId = UUID.randomUUID().toString();
        List<Map<String, Object>> successList = exchange.getProperty("successList", List.class);
        List<Map<String, Object>> failureList = exchange.getProperty("failureList", List.class);

        String status;
        if (!successList.isEmpty() && !failureList.isEmpty()) {
            status = "PARTIAL_SUCCESS";
        } else if (!successList.isEmpty()) {
            status = "SUCCESS";
        } else {
            status = "FAILED";
        }

        List<Map<String, Object>> allResults = new ArrayList<>();
        allResults.addAll(successList);
        allResults.addAll(failureList);

        Map<String, Object> resultDoc = new HashMap<>();
        resultDoc.put("_id", requestId);
        resultDoc.put("status", status);
        resultDoc.put("timestamp", LocalDateTime.now().toString());
        resultDoc.put("results", allResults);

        // Log the final result document
        exchange.getContext().createProducerTemplate().sendBody("log:FinalResult", "Final Status Document: " + resultDoc);

        exchange.getContext().createProducerTemplate()
                .sendBody("mongodb:myDb?database=mycartdb&collection=status&operation=save", resultDoc);
    }
}
