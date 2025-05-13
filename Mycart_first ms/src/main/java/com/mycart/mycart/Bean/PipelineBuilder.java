package com.mycart.mycart.Bean;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PipelineBuilder {

    private static final Logger logger = LoggerFactory.getLogger(PipelineBuilder.class);

    public List<Document> buildAggregation(String categoryId, boolean includeSpecial) {
        List<Document> pipeline = new ArrayList<>();
        Document matchStage = new Document("categoryId", categoryId);
        if (includeSpecial) {
            matchStage.append("specialProduct", new Document("$in", List.of(true, false)));
        } else {
            matchStage.append("specialProduct", false);
        }
        pipeline.add(new Document("$match", matchStage));
        pipeline.add(new Document("$lookup", new Document("from", "category")
                .append("localField", "categoryId")
                .append("foreignField", "_id")
                .append("as", "categoryDetails")));
        pipeline.add(new Document("$unwind", new Document("path", "$categoryDetails")
                .append("preserveNullAndEmptyArrays", true)));
        pipeline.add(new Document("$group", new Document("_id", "$categoryId")
                .append("categoryName", new Document("$first", "$categoryDetails.categoryName"))
                .append("categoryDepartment", new Document("$first", "$categoryDetails.department"))
                .append("items", new Document("$push", "$$ROOT"))));
        pipeline.add(new Document("$project", new Document("items.categoryDetails", 0)));
        logger.info("Aggregation pipeline: {}", pipeline);
        return pipeline;
    }
}
