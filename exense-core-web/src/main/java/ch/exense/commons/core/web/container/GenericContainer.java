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
package ch.exense.commons.core.web.container;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.exense.commons.app.Configuration;

public abstract class GenericContainer{
	
	private static final Logger logger = LoggerFactory.getLogger(GenericContainer.class);
	
	protected Configuration configuration;
	
	private ServerContext context;

	private ServiceRegistrationCallback serviceRegistrationCallback;
	
	public GenericContainer(Configuration configuration) {
		super();
		this.configuration = configuration;
		this.context = new ServerContext();
	}

	public void init(ServiceRegistrationCallback serviceRegistrationCallback) throws Exception {			
		this.serviceRegistrationCallback = serviceRegistrationCallback;

		initContext(context);
		
		context.setServiceRegistrationCallback(serviceRegistrationCallback);
		
		postInitContext(context);
	
	}
	
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public abstract void postInitContext(ServerContext context);

	public abstract void initContext(ServerContext context);


	public void destroy() {
		serviceRegistrationCallback.stop();
	}	
	public ServerContext getContext() {
		return context;
	}

	public abstract ContextHandler provideWebappContextHandler();

	public abstract void registerPotentialClasses(ResourceConfig resourceConfig);

	public abstract String provideServiceContextPath();	
}
