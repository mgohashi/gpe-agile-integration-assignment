/**
 *  Copyright 2005-2016 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package com.redhat.usecase;

import org.apache.camel.component.amqp.AMQPConnectionDetails;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.cxf.DataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sun.mdm.index.webservice.PersonEJB;

@SpringBootApplication
@Configuration
public class Application {
	
	@Value("${amq.host}")
	String amqHost;
	
	@Value("${amq.port}")
	String amqPort;
	
	@Value("${amq.username}")
	String amqUsername;
	
	@Value("${amq.password}")
	String amqPassword;
	
	@Value("${personEJBService.consumer.address}")
	private String address;

    /**
     * Main method to start the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    AMQPConnectionDetails securedAmqpConnection() {
      AMQPConnectionDetails amqpConnectionDetails = new AMQPConnectionDetails(
    		  String.format("amqp://%s:%s", amqHost, amqPort), amqUsername, amqPassword);
      return amqpConnectionDetails;
    }
    
    @Bean
    CxfEndpoint personServiceEndpoint() {
    	final CxfEndpoint cxfEndpoint = new CxfEndpoint();
        cxfEndpoint.setAddress(address);
        cxfEndpoint.setServiceClass(PersonEJB.class);
        cxfEndpoint.setDataFormat(DataFormat.PAYLOAD);
        cxfEndpoint.setBridgeErrorHandler(true);
        return cxfEndpoint;
    }

}