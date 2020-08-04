package ch.exense.commons.core.server;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.exense.commons.core.access.Secured;
import ch.exense.commons.core.web.container.ServerContext;

@Singleton
@Path("/demo2")
public class DemoRegistrable implements Registrable{

	@Inject
	ServerContext context;
	
	private static final Logger logger = LoggerFactory.getLogger(DemoRegistrable.class);
	
	static {
		logger.info("[Static]  Initializing demo registrable.");
	}
	
	@PostConstruct
	public void postConstruct() {
		logger.info("[@PostConstruct]  Initializing demo registrable.");
	}
	
	@Secured
	@GET
	//@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrders() {
		return Response.status(200).entity("{ \"context\" : \""+context+"\"}").build();
	}

}
