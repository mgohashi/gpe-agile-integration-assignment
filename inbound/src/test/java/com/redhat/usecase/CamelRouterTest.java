package com.redhat.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.customer.app.response.ESBResponse;

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
		camelContext.getRouteDefinitions().get(1).adviceWith(camelContext, new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				interceptSendToEndpoint("amqp:queue:q.empi.deim.in").skipSendToOriginalEndpoint()
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

		String person = IOUtils.toString(CamelRouterTest.class.getResource("/sample-person.xml"), "utf-8");

		HttpEntity<String> request = new HttpEntity<String>(person, headers);
		
		testRouteMessage.expectedBodiesReceived(person);

		// Then call the REST API
		ResponseEntity<ESBResponse> response = restTemplate.postForEntity("/camel/match", request, ESBResponse.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		ESBResponse esbResponse = response.getBody();
		assertThat(esbResponse).as("There was no response").isNotNull();

		testRouteMessage.assertIsNotSatisfied();
		assertThat(notify.matchesMockWaitTime()).as("Wait time does not match").isTrue();
	}
}