package com.mycart.mycart.Processors;

import com.mycart.mycart.Exception.InventoryException;
import jakarta.jms.Connection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class AMQHealthCheckProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connectionFactory.setUserName("admin");
            connectionFactory.setPassword("admin");

            Connection connection = connectionFactory.createConnection();
            connection.start();
            connection.close();
        } catch (Exception e) {
            throw new InventoryException("ActiveMQ has not been started yet.");
        }
    }
}
