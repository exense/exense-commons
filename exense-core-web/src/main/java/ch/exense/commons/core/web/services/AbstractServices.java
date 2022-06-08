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
package ch.exense.commons.core.web.services;

import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.web.container.ServerContext;
import ch.exense.commons.core.web.session.Session;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;

public abstract class AbstractServices {

	private static final String SESSION = "session";

	@Inject
	protected Configuration configuration;

	@Inject
	protected ServerContext context;
	
	@Inject 
	private HttpSession httpSession;
	
	public AbstractServices() {
		super();
	}
	
	protected ServerContext getContext() {
		return context;
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
