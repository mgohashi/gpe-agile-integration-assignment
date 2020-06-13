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
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spi.DataFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.customer.app.Person;
import com.customer.app.response.ESBResponse;

@Component
public class CamelRouter extends RouteBuilder {

	@Override
	public void configure() throws Exception {

		// @formatter:off
        restConfiguration()
            .component("servlet")
            .contextPath("/")
            .clientRequestValidation(true)
            .enableCORS(true)
            .bindingMode(RestBindingMode.xml);
        
        rest("/")
            .post("/match").description("Matches a parson")
            	.type(Person.class).consumes(MediaType.TEXT_XML_VALUE)
            	.outType(ESBResponse.class).produces(MediaType.TEXT_XML_VALUE)
                .route()
                	.to("direct:processMatch");

        from("direct:processMatch")
	        .routeId("processMatch").description("Process Match")
	    	.removeHeaders("*")
	    	.to("bean:DEIMService")
	    	.log(LoggingLevel.INFO, "com.redhat.usecase.route", "${body} - ESBResponse");
        
        JaxbDataFormat jaxb = new JaxbDataFormat("com.customer.app");
        jaxb.setMustBeJAXBElement(false);
        
        from("direct:integrateRoute")
        	.routeId("integrateRoute").description("DEIM Route")
        	.streamCaching()
	        .onException(Throwable.class)
		    	.handled(false)
		    	.log(LoggingLevel.ERROR, "com.redhat.usecase.route", "${exception}")
	    	.end()
            .log(LoggingLevel.DEBUG, "com.redhat.usecase.route", "${body} - Sending message to queue")
            .marshal(jaxb)
            .inOnly("amqp:queue:q.empi.deim.in")
            .transform(constant("2"));
        // @formatter:on
        
	}
	
}