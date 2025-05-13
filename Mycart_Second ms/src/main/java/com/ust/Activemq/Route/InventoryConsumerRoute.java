//package com.ust.Activemq.Route;
//
//import com.ust.Activemq.Exception.InventoryException;
//import com.ust.Activemq.Processor.*;
//import org.apache.camel.Exchange;
//import org.apache.camel.builder.RouteBuilder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class InventoryConsumerRoute extends RouteBuilder {
//
//
//
//    @Override
//    public void configure() throws Exception {
//
//        onException(InventoryException.class)
//                .handled(true)
//                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
//                .setBody(simple("{\"error\": \"${exception.message}\"}"))
//                .log("Exception caught: ${exception.message}")
//                .end();
//
//        onException(Throwable.class)
//                .handled(true)
//                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
//                .setBody(simple("{\"error\": \"Internal server error occurred.\"}"))
//                .log("Unhandled Exception: ${exception.message}")
//                .end();
//
//
//        from("activemq:queue:updateInventory")
//                .routeId("asyncInventoryProcessor1")
//                .log("📤 Consuming inventory update message from ActiveMQ queue")
//                .process(new InitProcessingListsProcessor())
//                .split(simple("${exchangeProperty.inventoryList}")).streaming()
//                .doTry()
//                .process(new ValidationProcessor())
//                .setBody(simple("${exchangeProperty.itemId}"))
//                .to("mongodb:myDb?database=mycartdb&collection=item&operation=findById")
//                .process(exchange -> {
//                    Object item = exchange.getMessage().getBody();
//                    String itemId = exchange.getProperty("itemId", String.class);
//                    if (item == null) {
//                        throw new InventoryException("Item not found with ID: " + itemId);
//                    }
//                })
//                .log("✅ Item found: ${body}")
//                .process(new UpdateStockProcessor())
//                .to("mongodb:myDb?database=mycartdb&collection=item&operation=save")
//                .log("✅ Item updated in db: ${body}")
//                .process(new SuccessResultProcessor())
//                .doCatch(InventoryException.class)
//                .process(new FailureResultProcessor())
//                .end()
//                .end()
//                .process(new FinalizeResultDocumentProcessor());
//    }
//}
package com.ust.Activemq.Route;

import com.ust.Activemq.Exception.InventoryException;
import com.ust.Activemq.Processor.*;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class InventoryConsumerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(InventoryException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(simple("{\"error\": \"${exception.message}\"}"))
                .log("Exception caught: ${exception.message}")
                .end();

        onException(Throwable.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("{\"error\": \"Internal server error occurred.\"}"))
                .log("Unhandled Exception: ${exception.message}")
                .end();

        from("activemq:queue:updateInventory")
                .routeId("asyncInventoryProcessor1")
                .log("📤 Consuming inventory update message from ActiveMQ queue")
                .process(new InitProcessingListsProcessor())
                .split(simple("${exchangeProperty.inventoryList}"))
                .parallelProcessing() // ✅ Enable parallel processing
                .streaming()
                .doTry()
                .process(new ValidationProcessor())
                .setBody(simple("${exchangeProperty.itemId}"))
                .to("mongodb:myDb?database=mycartdb&collection=item&operation=findById")
                .process(exchange -> {
                    Object item = exchange.getMessage().getBody();
                    String itemId = exchange.getProperty("itemId", String.class);
                    if (item == null) {
                        throw new InventoryException("Item not found with ID: " + itemId);
                    }
                })
                .log("✅ Item found: ${body}")
                .process(new UpdateStockProcessor())
                .to("mongodb:myDb?database=mycartdb&collection=item&operation=save")
                .log("✅ Item updated in db: ${body}")
                .process(new SuccessResultProcessor())
                .doCatch(InventoryException.class)
                .process(new FailureResultProcessor())
                .end()
                .end()
                .process(new FinalizeResultDocumentProcessor());
    }
}
