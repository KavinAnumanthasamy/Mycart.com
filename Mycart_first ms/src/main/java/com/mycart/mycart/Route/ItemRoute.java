package com.mycart.mycart.Route;

import com.mongodb.MongoBulkWriteException;
import com.mycart.mycart.Bean.PipelineBuilder;
            import com.mycart.mycart.Exception.InventoryException;
            import com.mycart.mycart.Processors.*;
            import org.apache.camel.Exchange;
            import org.apache.camel.builder.RouteBuilder;
            import org.apache.camel.model.rest.RestBindingMode;
            import org.apache.camel.model.rest.RestParamType;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.springframework.beans.factory.annotation.Value;
            import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemRoute extends RouteBuilder {

    @Value("${app.error.itemNotFound}")
    private String itemNotFoundMessage;

    @Value("${app.error.categoryNotFound}")
    private String categoryNotFoundMessage;

    @Override
    public void configure() {
        Logger logger = LoggerFactory.getLogger(ItemRoute.class);


        onException(InventoryException.class)
                .handled(true)
                .log("Error: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(simple("${exception.message}"));

        onException(Exception.class)
                .handled(true)
                .log("Error: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("Internal server error: ${exception.message}"));

        onException(Throwable.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("{\"error\": \"Internal server error occurred.\"}"))
                .log("Unhandled Exception: ${exception.message}")
                .end();

        onException(com.mongodb.MongoWriteException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .process(exchange -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "failed");
                    response.put("message", "Already exists");
                    exchange.getMessage().setBody(response);
                });

        restConfiguration()
                .component("netty-http")
                .host("0.0.0.0")//for all available networks
                .port(8080)
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");

        //===============================✅ Add New Item=======================================================================================
//    (InvalidPriceProcessor,ItemAlreadyExistsProcessor,CategoryNotFoundProcessor,ItemInsertedSuccessProcessor)

        rest("/additem")
                .post()
                .consumes("application/json")
                .to("direct:postNewItem");

        from("direct:postNewItem")
                .log("Received: ${body}")
                .setProperty("item", body())
                .choice()
                .when(simple("${body[itemPrice][basePrice]} <= 0 || ${body[itemPrice][sellingPrice]} <= 0"))
                .process(new InvalidPriceProcessor())
                .stop()
                .end()
                .setHeader("CamelMongoDbCriteria", simple("{ \"_id\": \"${body[_id]}\" }"))
                .to("mongodb:myDb?database=mycartdb&collection=item&operation=findById")
                .choice()
                .when(body().isNotNull())
                .process(new ItemAlreadyExistsProcessor())
                .stop()
                .end()
                .setBody(simple("${exchangeProperty.item[categoryId]}"))
                .to("mongodb:myDb?database=mycartdb&collection=category&operation=findById")
                .choice()
                .when(body().isNull())
                .process(new CategoryNotFoundProcessor())
                .stop()
                .end()
                .setBody(exchangeProperty("item"))
                .to("mongodb:myDb?database=mycartdb&collection=item&operation=insert")
                .process(new ItemInsertedSuccessProcessor());


        //====================================================  Add New Category=========================================================================

        rest("/category").post("/add")
                .consumes("application/json")
                .produces("application/json")
                .to("direct:addCategory");

        from("direct:addCategory")
                .process(exchange -> {
                    Object body = exchange.getIn().getBody();
                    if (body instanceof List) { // instance-> will check the elemnts inside the body is list or not!
                        throw new InventoryException("Multiple categories not allowed.");
                    }
                })
                .log("Inserting new category: ${body}")
                .to("mongodb:myDb?database=mycartdb&collection=category&operation=insert")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .process(exchange -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Category inserted successfully");
                    exchange.getMessage().setBody(response);
                });

        //================================================  Get all items=================================================
        rest("/mycart/items")
                .get()
                .description("Get all items from the database")
                .produces("application/json")
                .to("direct:getAllItems");

        from("direct:getAllItems")
                .log("Fetching all items from DB")
                .to("mongodb:myDb?database=mycartdb&collection=item&operation=findAll")
                .choice()
                .when(simple("${body.size} == 0 || ${body} == null"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .process(exchange -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("message", "No items found in the database.");
                    exchange.getMessage().setBody(errorResponse);
                })
                .otherwise()
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .log("Items found: ${body.size}")
                .end();


        //===================================✅ Get Item by ID (Requirement 1.1) ✅===================================================
        rest("/mycart/item/{itemId}")
                .get()
                .produces("application/json")
                .to("direct:getItemById");

        from("direct:getItemById")
                .log("Fetching item with ID: ${header.itemId}")
                .setBody(simple("${header.itemId}"))
                .to("mongodb:myDb?database=mycartdb&collection=item&operation=findById")
                .choice()
                .when(body().isNull())
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .process(exchange -> {
                    Map<String, String> errorResp = new HashMap<>();
                    errorResp.put("status", "error");
                    errorResp.put("message", "Item not found in DataBase");
                    exchange.getMessage().setBody(errorResp);
                })
                .otherwise()
                .process(exchange -> {
                    Map<String, Object> item = exchange.getIn().getBody(Map.class);
                    Object categoryId = item.get("categoryId");
                    exchange.setProperty("itemData", item);
                    exchange.setProperty("categoryId", categoryId);
                })
                .setBody(simple("${exchangeProperty.categoryId}"))
                .to("mongodb:myDb?database=mycartdb&collection=category&operation=findById")
                .process(new ItemWithCategoryResponseProcessor())
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .log("Final item response: ${body}")
                .end();

        //======================================= Get Category by ID===================================================

        rest("/category")
                .get("/{id}")
                .produces("application/json")
                .to("direct:getCategoryById");

        from("direct:getCategoryById")
                .log("Fetching category by ID: ${header.id}")
                .setBody(simple("${header.id}"))  // Set ID for findById
                .to("mongodb:myDb?database=mycartdb&collection=category&operation=findById")
                .choice()
                .when(body().isNull())
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .process(exchange -> {
                    Map<String, String> errorResp = new HashMap<>();
                    errorResp.put("status", "error");
                    errorResp.put("message", "Category not found");
                    exchange.getMessage().setBody(errorResp);
                })
                .log("Category not found for ID: ${header.id}")
                .otherwise()
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .log("Category found: ${body}")
                .end();


        //==============================================✅ Get Items by Category ✅==========================================
        //                                              (ItemsResponseProcessor)

        rest("/mycart/items/{categoryId}")
                .get()
                .param()
                .name("includeSpecial").type(RestParamType.query)
                .defaultValue("false")
                .description("Include special items")
                .endParam()
                .produces("application/json")
                .to("direct:getItemsByCategory");

        from("direct:getItemsByCategory")
                // Convert includeSpecial header String to boolean before calling bean
                .process(exchange -> {
                    String includeSpecialStr = exchange.getIn().getHeader("includeSpecial", String.class);
                    boolean includeSpecialBool = "true".equalsIgnoreCase(includeSpecialStr);
                    exchange.getIn().setHeader("includeSpecialBool", includeSpecialBool);
                })
                // Call your bean with boolean param now
                .bean(PipelineBuilder.class, "buildAggregation(${header.categoryId}, ${header.includeSpecialBool})")
                .to("mongodb:myDb?database=mycartdb&collection=item&operation=aggregate")
                .process(new ItemsResponseProcessor());


        //=======================================Get all categories=============================================================
        //                                   (GetAllCategoryProcessor)

        rest("/mycart/categories")
                .get()
                .description("Get all categories from the database")
                .produces("application/json")
                .to("direct:getAllCategories");

        from("direct:getAllCategories")
                .log("Fetching all categories from DB")
                .to("mongodb:myDb?database=mycartdb&collection=category&operation=findAll")
                .process(new GetAllCategoryProcessor());


        //===================================✅ Update Inventory Requirement1 ✅=============================================================
//(InventoryValidationProcessor,FlattenInventoryProcessor,SplitInventoryProcessor,FetchItemFromDBProcessor,UpdateStockProcessor)

        rest("/inventory/update")
                .post()
                .to("direct:updateInventory");

        from("direct:updateInventory")
                .process(new InventoryValidationProcessor()) // ✅ Combined validation
                .process(new FlattenInventoryProcessor())
                .process(new SplitInventoryProcessor())
                .split(simple("${exchangeProperty.inventoryList}")).streaming().stopOnException()
                .setProperty("itemId", simple("${body['_id']}"))
                .setProperty("soldOut", simple("${body['stockDetails']['soldOut']}"))
                .setProperty("damaged", simple("${body['stockDetails']['damaged']}"))
                .setBody(simple("${exchangeProperty.itemId}"))
                .to("mongodb:myDb?database=mycartdb&collection=item&operation=findById")
                .process(new FetchItemFromDBProcessor())
                .process(new UpdateStockProcessor())
                .to("mongodb:myDb?database=mycartdb&collection=item&operation=save")
                .setBody(simple("{\"message\": \"Inventory updated successfully for item ${exchangeProperty.itemId}\", \"updatedStock\": ${exchangeProperty.updatedItem[stockDetails][availableStock]}}"))
                .log("Updated inventory for ${exchangeProperty.itemId}")
                .end();


        //===================================✅ Update Inventory Requirement2 ✅=============================================================
        //                 (AMQHealthCheckProcessor,AMQExtractItemsArrayProcessor)

        rest("/inventory/async")
                .post()
                .description("Posting the items for the update in activemq")
                .produces("application/json")
                .to("direct:inventoryUpdateEntryPoint");

        from("direct:inventoryUpdateEntryPoint")
                .process(new AMQHealthCheckProcessor()) // Check AMQ connection
                .choice()
                .when(simple("${body[status]} == 'DOWN'"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(503))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .stop()
                .otherwise()
                .log("📥 Received inventory update request")
                .process(new AMQExtractItemsArrayProcessor()) // Extract "items" array
                .process(exchange -> {
                    List<?> items = exchange.getIn().getBody(List.class);
                    exchange.setProperty("itemCount", items.size());
                })
                .log(" Sending items to ActiveMQ as one message")
                .to("activemq:queue:updateInventory?exchangePattern=InOnly&deliveryMode=2")
                .log(" Inventory message sent to ActiveMQ. Items count: ${exchangeProperty.itemCount}")
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                .process(exchange -> { //<--- Lambda process instead of this we can use seperate class
                    int count = exchange.getProperty("itemCount", Integer.class);
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "accepted");
                    response.put("message", "Inventory array sent to ActiveMQ");
                    exchange.getMessage().setBody(response);
                });
    }
}