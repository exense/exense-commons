package ch.exense.commons.core.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.exense.commons.core.web.container.ServerContext;

@Singleton
@Path("/demo2")
public class DemoRegistrable implements Registrable{

	@Inject
	ServerContext context;
	
	static {
		try {
			System.out.println("------------->  Initializing demo registrable.");
		}finally{}
	}
	
	@GET
	//@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOrders() {
		return Response.status(200).entity("{ \"context\" : \""+context+"\"}").build();
	}

}
