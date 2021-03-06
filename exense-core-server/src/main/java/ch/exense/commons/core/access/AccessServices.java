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
package ch.exense.commons.core.access;

import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.commons.auth.Credentials;
import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.access.authentication.AuthenticationManager;
import ch.exense.commons.core.access.role.Role;
import ch.exense.commons.core.access.role.RoleProvider;
import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;
import ch.exense.commons.core.model.user.User;
import ch.exense.commons.core.web.services.AbstractServices;
import ch.exense.commons.core.web.session.Session;

@Singleton
@Path("/access")
public class AccessServices extends AbstractServices {
	private static Logger logger = LoggerFactory.getLogger(AccessServices.class);
	
	@Inject
	private RoleProvider roleProvider;
	
	@Inject
	private AuthenticationManager authenticationManager;
	
	@Inject
	private AccessManager accessManager;
	
	public AccessServices() {
		super();
	}
	
	@PostConstruct
	public void init() throws Exception {
		logger.info("AccessServices post constructed successfully.");
	}
	
	public static class SessionResponse {
		
		private String username;
		private Role role;
		
		public SessionResponse(String username, Role role) {
			super();
			this.username = username;
			this.role = role;
		}

		public String getUsername() {
			return username;
		}

		public Role getRole() {
			return role;
		}
	}

	@POST
	@Path("/login")
    @Produces("application/json")
    @Consumes("application/json")
    public Response authenticateUser(Credentials credentials) {
		Session session = getSession();
		boolean authenticated = false;
		try {
			authenticated = authenticationManager.authenticate(session, credentials);
		}catch(Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity("Authentication failed. Check the server logs for more details.").type("text/plain").build();
		}
        if(authenticated) {
        	SessionResponse sessionResponse = buildSessionResponse(session);
        	return Response.ok(sessionResponse).build();            	
        } else {
        	return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).entity("Invalid username/password").type("text/plain").build();
        }    
    }
	
	@GET
	@Secured
	@Path("/session")
	public SessionResponse getCurrentSession() {
		Session session = getSession();
		return buildSessionResponse(session);
	}
	
	// TODO Reimplement this method as it isn't working anymore since the sessions are now managed by the web server
	//Not "Secured" on purpose:
	//we're allow third parties to loosely check the validity of a token in an SSO context
//	@GET
//	@Path("/checkToken")
//	public Boolean isValidToken(@QueryParam("token") String token) {
//		logger.debug("Token " + token + " is valid.");
//		return true;
//	}

	protected SessionResponse buildSessionResponse(Session session) {
		User user = session.getUser();
		Role role = accessManager.getRoleInContext(session);
		return new SessionResponse(user.getUsername(), role);
	}
	
	@GET
	@Path("/conf")
	public AccessConfiguration getAccessConfiguration() {
		AccessConfiguration conf = new AccessConfiguration();
		conf.setDemo(isDemo());
		conf.setAuthentication(authenticationManager.useAuthentication());
		conf.setRoles(roleProvider.getRoles().stream().map(r->r.getAttributes().get(AbstractOrganizableObject.NAME)).collect(Collectors.toList()));
		
		// conf should cover more than just AccessConfiguration but we'll store the info here for right now
		Configuration ctrlConf = getContext().getConfiguration();
		conf.getMiscParams().put("enforceschemas", getContext().getConfiguration().getProperty("enforceschemas", "false"));

		if(ctrlConf.hasProperty("ui.default.url")) {
			conf.setDefaultUrl(ctrlConf.getProperty("ui.default.url"));
		}
		conf.setDebug(ctrlConf.getPropertyAsBoolean("ui.debug", false));
		conf.setTitle(ctrlConf.getProperty("ui.title", "STEP"));
		return conf;
	}
	
	@POST
	@Secured
	@Path("/logout")
    public void logout() {
		setSession(null);
    }
	
	public boolean isDemo() {
		return configuration.getPropertyAsBoolean("demo", false);
	}
}
