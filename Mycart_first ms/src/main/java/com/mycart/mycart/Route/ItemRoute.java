        package com.mycart.mycart.Route;

        import com.mycart.mycart.Bean.PipelineBuilder;
    //    import com.mycart.mycart.Dummy.InitProcessingListsProcessor;
    //    import com.mycart.mycart.Dummy.InventoryProcessorsActiveMq;
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

                restConfiguration()
                        .component("netty-http")
                        .host("0.0.0.0")
                        .port(8080)
                        .bindingMode(RestBindingMode.json)
                        .dataFormatProperty("prettyPrint", "true");

                //================================================================= POST =========================================================================================

                //===============================✅ Add New Item=======================================================================================

                rest("/additem")
                        .post().consumes("application/json")
                        .to("direct:postNewItem");

                from("direct:postNewItem")
                        .log("Received: ${body}")
                        .setProperty("item", body())
                        .choice()
                        .when(simple("${body[itemPrice][basePrice]} <= 0 || ${body[itemPrice][sellingPrice]} <= 0"))
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                        .setBody(constant("BasePrice and sellingPrice must be greater than 0"))
                        .stop()
                        .end()
                        .setHeader("CamelMongoDbCriteria", simple("{ \"_id\": \"${body[_id]}\" }"))
                        .to("mongodb:myDb?database=mycartdb&collection=item&operation=findById")
                        .choice()
                        .when(body().isNotNull())
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                        .setBody(constant("Item already exists"))
                        .otherwise()
                        .setBody(exchangeProperty("item"))
                        .to("mongodb:myDb?database=mycartdb&collection=item&operation=insert")
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                        .setBody(constant("Item inserted successfully"));

                //==================================================== ✅ Add New Category=========================================================================

                rest("/category").post("/add")
                        .consumes("application/json")
                        .produces("application/json")
                        .to("direct:addCategory");

                from("direct:addCategory")
                        .log("Inserting new category: ${body}")
                        .to("mongodb:myDb?database=mycartdb&collection=category&operation=insert")
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                        .setBody().simple("✅ Category inserted successfully");

                //=============================================// ✅ REST Endpoint for Bulk Insert from REST Body============================================================================

                rest("/mycart")
                        .post("/bulk/items")
                        .consumes("application/json")
                        .produces("application/json")
                        .to("direct:bulkInsertFromRest");

                from("direct:bulkInsertFromRest")
                        .routeId("bulkInsertFromRest")
                        .log("Received bulk insert request from REST: ${body}")
                        .split(body())
                        .to("mongodb:myDb?database=mycartdb&collection=item&operation=insert")
                        .log("Inserted item: ${body}")
                        .end()
                        .setBody(constant("✅ Bulk insert completed via REST API"));

                //============================================================= ✅ Get all items=================================================

                rest("/mycart/items")
                        .get()
                        .description("Get all items from the database")
                        .to("direct:getAllItems");

                from("direct:getAllItems")
                        .log("Fetching all items from DB")
                        .to("mongodb:myDb?database=mycartdb&collection=item&operation=findAll")
                        .choice()
                        .when(body().method("isEmpty"))
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                        .setBody(constant("No items found in the database."))
                        .otherwise()
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                        .log("Items found: ${body.size}")
                        .end();

                //===================================✅ Get Item by ID===================================================

                rest("/mycart/item/{itemId}")
                        .get()
                        .to("direct:getItemById");

                from("direct:getItemById")
                        .log("Fetching item with ID: ${header.itemId}")
                        .setBody(simple("${header.itemId}"))  // ✅ Set body with itemId for findById
                        .to("mongodb:myDb?database=mycartdb&collection=item&operation=findById")
                        .choice()
                        .when(body().isNull())
                        .log("Item not found")
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                        .setBody(constant("Item not found in inventory."))
                        .otherwise()
                        .log("Item found: ${body}")
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                        .end();

                //=============================================================✅ Get Category by ID===================================================

                rest("/category")
                        .get("/{id}")
                        .produces("application/json")
                        .to("direct:getCategoryById");

                from("direct:getCategoryById")
                        .log("Fetching category by ID: ${header.id}")
                        .setBody(simple("${header.id}"))  // ✅ Set the ID in body for findById
                        .to("mongodb:myDb?database=mycartdb&collection=category&operation=findById")
                        .choice()
                        .when(body().isNull())
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                        .setBody(constant("Category not found" ))  // Return error message in JSON format
                        .log("Category not found for ID: ${header.id}")
                        .otherwise()
                        .log("Category found: ${body}")
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                        .end();


                //==============================================✅ Get Items by Category==========================================
                rest("/mycart/items/{categoryId}")
                        .get()
                        .param()
                        .name("includeSpecial").type(RestParamType.query)
                        .defaultValue("false").description("Include special items")
                        .endParam()
                        .to("direct:getItemsByCategory");

                from("direct:getItemsByCategory")
                        .bean(PipelineBuilder.class, "buildAggregation(${header.categoryId}, ${header.includeSpecial})")
                        .to("mongodb:myDb?database=mycartdb&collection=item&operation=aggregate")
                        .choice()
                        .when(body().method("isEmpty"))
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                        .setBody(simple("No items found for the given category."))
                        .otherwise()
                        .setBody(simple("${body[0]}"));

                //=======================================Get all categories=============================================================

                rest("/mycart/categories")
                        .get()
                        .description("Get all categories from the database")
                        .to("direct:getAllCategories");

                from("direct:getAllCategories")
                        .log("Fetching all categories from DB")
                        .to("mongodb:myDb?database=mycartdb&collection=category&operation=findAll")
                        .choice()
                        .when(body().method("isEmpty"))
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                        .setBody(constant("No categories found in the database."))
                        .otherwise()
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                        .log("Categories found: ${body.size}")
                        .end();


                //===================================✅ Update Inventory Requirement1=============================================================

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
                        .log("Updated inventory for ${exchangeProperty.itemId} with availableStock: ${exchangeProperty.updatedItem[stockDetails][availableStock]}")
                        .end();


                //===================================✅ Update Inventory Requirement2=============================================================
                rest("/inventory/update2")
                        .post()
                        .to("direct:inventoryUpdateEntryPoint");

                from("direct:inventoryUpdateEntryPoint")
                        .process(new AMQHealthCheckProcessor())
                        .log("📥 Received inventory update request")
                        .process(new AMQFlattenItemListProcessor())
                        .split(body())
                        .parallelProcessing()
                        .streaming()
                        .to("activemq:queue:updateInventory?exchangePattern=InOnly&deliveryMode=2")
                        .end()
                        .log("📬 All inventory items processed")
                        .setBody(constant("📨 Inventory update request accepted for asynchronous processing"))
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                        .end();

            }
        }