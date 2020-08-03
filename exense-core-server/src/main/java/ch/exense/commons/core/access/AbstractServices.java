/*******************************************************************************
 * (C) Copyright 2016 Jerome Comte and Dorian Cransac
 *  
 * This file is part of STEP
 *  
 * STEP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * STEP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with STEP.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ch.exense.commons.core.access;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.server.ExenseServer;
import ch.exense.commons.core.server.ServerContext;
import ch.exense.commons.core.server.security.Session;

public abstract class AbstractServices {

	private static final String SESSION = "session";

	@Inject
	protected ExenseServer server;
	
	@Inject 
	private HttpSession httpSession;
	
	protected Configuration configuration;

	public AbstractServices() {
		super();
	}
	
	@PostConstruct
	public void init() throws Exception {
		configuration = server.getContext().getConfiguration();
	}

	protected ServerContext getContext() {
		return server.getContext();
	}

	protected Session getSession() {
		if(httpSession != null) {
			return (Session) httpSession.getAttribute(SESSION);
		} else {
			return null;
		}
	}
	
	protected void setSession(Session session) {
		httpSession.setAttribute(SESSION, session);
	}
}