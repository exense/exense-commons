package ch.exense.commons.core.server.security;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ExtendedUriInfo;

import ch.exense.commons.core.web.container.ServerContext;
import ch.exense.commons.core.web.services.AbstractServices;
import ch.exense.commons.core.web.session.Session;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter extends AbstractServices implements ContainerRequestFilter {
	
	@Inject
	private ExtendedUriInfo extendendUriInfo;
	
	private AuthenticationManager authenticationManager;
	private AccessManager accessManager;
	
	@PostConstruct
	public void init() throws Exception {
		ServerContext context = getContext();
		accessManager = context.get(AccessManager.class);
		//TODO
		//authenticationManager = context.get(AuthenticationManager.class);
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// Retrieve or initialize session
		Session session = retrieveOrInitializeSession();

		//authenticationManager.authenticateDefaultUserIfAuthenticationIsDisabled(session);
		
		// Check rights
		Secured annotation = extendendUriInfo.getMatchedResourceMethod().getInvocable().getHandlingMethod().getAnnotation(Secured.class);
		if(annotation != null) {
			if(session.isAuthenticated()) {
				String right = annotation.right();
				if(right.length()>0) {
					boolean hasRight = accessManager.checkRightInContext(session, right);
					if(!hasRight) {
						requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
					}
				}
			} else {
				requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
			}
		}
	}
	
	protected Session retrieveOrInitializeSession() {
		Session session = getSession();
		if(session == null) {
			session = new Session();
			setSession(session);
		}
		return session;
	}
}