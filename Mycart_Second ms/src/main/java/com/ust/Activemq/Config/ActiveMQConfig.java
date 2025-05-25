package com.ust.Activemq.Config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActiveMQConfig {

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        factory.setTrustedPackages(java.util.Arrays.asList(
                "java.util",
                "java.lang",
                "com.your.package.model" // your custom classes if any
        ));
        return factory;
    }
}
