/*******************************************************************************
 * Copyright 2021 exense GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package ch.exense.commons.core.web.container;

import javax.inject.Inject;
import javax.inject.Singleton;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Path("/public")
public class DemoPublicServices{

	@Inject
	ServerContext context;
	
	private static final Logger logger = LoggerFactory.getLogger(DemoPublicServices.class);
	
	static {
		logger.info("[Static]  Initializing demo services.");
	}
	
	@PostConstruct
	public void postConstruct() {
		logger.info("[@PostConstruct]  Initializing demo services.");
	}
	
	@GET
	//@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrders() {
		return Response.status(200).entity("{ \"service\" : \"public\", \"context\" : \""+context+"\"}").build();
	}

}
