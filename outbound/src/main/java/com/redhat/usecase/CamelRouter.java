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

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.stereotype.Component;

/**
 * A simple Camel REST DSL route that implements the greetings service.
 * 
 */
@Component
public class CamelRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {
    	DataFormat jaxb = new JaxbDataFormat("com.sun.mdm.index.webservice");
    	
        // @formatter:off
    	onException(Throwable.class)
    		.handled(true)
    		.maximumRedeliveries(3)
    		.maximumRedeliveryDelay(2000L)
    		.useOriginalMessage()
    		.log(LoggingLevel.DEBUG, "com.redhat.usecase.route", "Cause: ${exception}")
    		.choice()
    			.when(simple("${exception} != null && ${exception.class.name} == 'org.apache.cxf.transport.http.HTTPException'"))
    				.setHeader("Exception_Details", simple("${exception.class.name} - ${exception.message}"))
					.log(LoggingLevel.ERROR, "com.redhat.usecase.route", "${body} - Error calling Web Service. Try to see if the endpoint is correct.")
    			.when(simple("${exception} != null && ${exception?.cause?.class.name} == 'java.net.ConnectException'"))
    				.setHeader("Exception_Details", simple("${exception?.cause?.class.name} - ${exception?.cause?.message}"))
    				.log(LoggingLevel.ERROR, "com.redhat.usecase.route", "${body} - Error connecting to Web Service. Try to see if the port is open.")
    			.otherwise()
    				.setHeader("Exception_Details", simple("${exception.class.name} - ${exception.message}"))
    				.log(LoggingLevel.ERROR, "com.redhat.usecase.route", "${body} - General Error")
				.endChoice()
    		.end()
    		.log(LoggingLevel.DEBUG, "com.redhat.usecase.route", "${body} - Sending to 'q.empi.nextgate.dlq' queue")
    		.to("amqp:queue:q.empi.nextgate.dlq");
    	
        from("amqp:queue:q.empi.nextgate.out")
        	.log(LoggingLevel.DEBUG, "com.redhat.usecase.route", "[${body}] Received MSG")
        	.unmarshal(jaxb)
        	.log(LoggingLevel.DEBUG, "com.redhat.usecase.route", "${body} - Message received unmarshaled")
        	.setHeader(CxfConstants.OPERATION_NAME, constant("executeMatchUpdate"))
        	.to("cxf:bean:personServiceEndpoint")
        	.log(LoggingLevel.DEBUG, "com.redhat.usecase.route", "${body} - Response from the Web Service");
        // @formatter:on
    }

}