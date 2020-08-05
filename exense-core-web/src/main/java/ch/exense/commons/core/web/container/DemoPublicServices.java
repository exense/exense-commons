package ch.exense.commons.core.web.container;

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
