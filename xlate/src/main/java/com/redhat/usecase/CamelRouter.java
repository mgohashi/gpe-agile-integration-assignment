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
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.springframework.stereotype.Component;

import com.sun.mdm.index.webservice.ExecuteMatchUpdate;

@Component
public class CamelRouter extends RouteBuilder {

    @Override
    public void configure() throws Exception {

    	DataFormat jaxbCustomerApp = new JaxbDataFormat("com.customer.app");
    	JaxbDataFormat jaxbWebservice = new JaxbDataFormat("com.sun.mdm.index.webservice");
    	jaxbWebservice.setMustBeJAXBElement(false);
    	
        // @formatter:off
    	onException(Throwable.class)
			.handled(true)
			.maximumRedeliveries(3)
			.maximumRedeliveryDelay(2000L)
			.useOriginalMessage()
			.log(LoggingLevel.DEBUG, "com.redhat.usecase.route", "Cause: ${exception}")
			.choice()
				.when(simple("${exception} != null && ${exception.class.name} == 'org.apache.camel.InvalidPayloadException'"))
					.log(LoggingLevel.ERROR, "com.redhat.usecase.route", "[${body}] - Error transforming the input message. Try to see if the message is correct.")
				.otherwise()
					.log(LoggingLevel.ERROR, "com.redhat.usecase.route", "[${body}] - General Error")
				.endChoice()
			.end()
			.setHeader("Exception_Details", simple("${exception.class.name} - ${exception.message}"))
			.log(LoggingLevel.INFO, "com.redhat.usecase.route", "Message [${body}] - Sending to 'q.empi.transform.dlq' queue")
			.to("amqp:queue:q.empi.transform.dlq");
    	
        from("amqp:queue:q.empi.deim.in").description("Processing DEIM messages")
            .streamCaching()
            .log(LoggingLevel.DEBUG, "com.redhat.usecase", "[${body}] Received MSG")
            .unmarshal(jaxbCustomerApp)
            .convertBodyTo(ExecuteMatchUpdate.class)
            .log(LoggingLevel.DEBUG, "com.redhat.usecase", "[${body}] Message converted")
            .marshal(jaxbWebservice)
            .log(LoggingLevel.DEBUG, "com.redhat.usecase", "[${body}] Message marshaled to XML")
            .inOnly("amqp:queue:q.empi.nextgate.out");
        // @formatter:on
    }

}