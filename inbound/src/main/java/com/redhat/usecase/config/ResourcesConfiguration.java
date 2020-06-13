package com.redhat.usecase.config;

import org.apache.camel.component.amqp.AMQPConnectionDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourcesConfiguration {
	@Value("${amq.host}")
	String amqHost;
	
	@Value("${amq.port}")
	String amqPort;
	
	@Value("${amq.username}")
	String amqUsername;
	
	@Value("${amq.password}")
	String amqPassword;
	
	@Bean
    AMQPConnectionDetails securedAmqpConnection() {
      AMQPConnectionDetails amqpConnectionDetails = new AMQPConnectionDetails(
    		  String.format("amqp://%s:%s", amqHost, amqPort), amqUsername, amqPassword);
      return amqpConnectionDetails;
    }
}
