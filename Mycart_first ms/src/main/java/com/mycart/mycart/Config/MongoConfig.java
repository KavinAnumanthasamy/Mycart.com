package com.mycart.mycart.Config;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.camel.component.mongodb.MongoDbComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongo() {
        return MongoClients.create(mongoUri);
    }


    @Bean("myDb")
    public MongoDbComponent mongoDbComponent(MongoClient mongo) {
        MongoDbComponent mongoDbComponent = new MongoDbComponent();
        mongoDbComponent.setMongoConnection(mongo);
        return mongoDbComponent;
    }


}
