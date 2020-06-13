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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

import com.customer.app.Person;
import com.customer.app.response.ESBResponse;

@Service("DEIMService")
public class DEIMServiceImpl implements DEIMService {

	@Produce(uri = "direct:integrateRoute")
	ProducerTemplate template;

	@Override
	public ESBResponse match(Person person) {

		// This header is used to direct the message in the Camel route
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("METHOD", "match");

		String comment = "NONE";
		
		ESBResponse esbResponse = new ESBResponse();
		
		try {
			String camelResponse = template.requestBodyAndHeaders(template.getDefaultEndpoint(), person, headers,
					String.class);

			esbResponse.setBusinessKey(UUID.randomUUID().toString());
			esbResponse.setPublished(true);

			// Here we hard code the response code values to strings for the demo
			// A better practice would be to have an ENUM class
			
			if (camelResponse.equals("0")) {
				comment = "NO MATCH";
			} else if (camelResponse.equals("1")) {
				comment = "MATCH";
			} else if (camelResponse.equals("2")) {
				comment = "DONE";
			} else {
				comment = "ERROR";
			}

		} catch (Exception e) {
			comment = "ERROR";
			e.printStackTrace();
		}

		esbResponse.setComment(comment);

		return esbResponse;
	}

}