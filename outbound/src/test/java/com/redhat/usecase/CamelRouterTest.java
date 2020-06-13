package com.redhat.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = { Application.class })
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CamelRouterTest {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private CamelContext camelContext;
	
	@EndpointInject(uri = "mock:testRouteMessage")
    protected MockEndpoint testRouteMessage;

	@Test
	public void matchTest() throws Exception {
		camelContext.removeComponent("amqp");
		camelContext.addComponent("amqp", this.camelContext.getComponent("seda"));
	
		camelContext.getRouteDefinitions().get(0).adviceWith(camelContext, new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptSendToEndpoint("cxf:bean:personServiceEndpoint").skipSendToOriginalEndpoint()
						.setExchangePattern(ExchangePattern.InOnly)
						.log(LoggingLevel.INFO, "${body} - Intercepted")
						.to("mock:testRouteMessage");
			}
		});
		
		testRouteMessage.setExpectedMessageCount(1);

		NotifyBuilder notify = new NotifyBuilder(camelContext).whenCompleted(1).create();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Accept", "text/xml");
		headers.add("Content-Type", "text/xml");

		String executeMatchUpdate = IOUtils.toString(CamelRouterTest.class.getResource("/sample-executeMatchUpdate.xml"), "utf-8");

		testRouteMessage.expectedBodiesReceived(executeMatchUpdate);
		
		ProducerTemplate template = camelContext.createProducerTemplate();
		
		template.sendBody("amqp:queue:q.empi.nextgate.out", executeMatchUpdate);

		testRouteMessage.assertIsNotSatisfied();
		assertThat(notify.matchesMockWaitTime()).as("Wait time does not match").isTrue();
	}
}