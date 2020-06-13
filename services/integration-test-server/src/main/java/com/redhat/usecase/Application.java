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

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.company.app.test.PersonEJBImpl;
import com.sun.mdm.index.webservice.PersonEJB;

@SpringBootApplication
@Configuration
public class Application {

	@Autowired
	private Bus bus;
	
	@Value("${personEJBService.producer.address}")
	private String address;
	
    /**
     * Main method to start the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    Endpoint personServiceEndpoint(PersonEJB personEJB) {
    	EndpointImpl endpoint = new EndpointImpl(bus, personEJB);
    	endpoint.publish(address);
    	endpoint.setEndpointName(new QName("http://webservice.index.mdm.sun.com/", "PersonEJBPort"));
    	endpoint.setServiceName(new QName("http://webservice.index.mdm.sun.com/", "PersonEJBService"));
    	endpoint.setWsdlLocation("wsdl/EMPI_18080_2.wsdl");
    	return endpoint;
    }
    
    @Bean
    PersonEJB personEJB() {
    	return new PersonEJBImpl();
    }
    
}