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
package ch.exense.commons.core.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.exense.commons.core.mongo.accessors.generic.MongoClientSession;
import ch.exense.commons.core.server.security.SecurityFilter;
import ch.exense.commons.core.web.container.AbstractJettyContainer;

public abstract class FullFeaturedServer extends AbstractJettyContainer{

	protected MongoClientSession session;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(FullFeaturedServer.class);
	
	public FullFeaturedServer() {
		super();
	}
	
	@Override
	protected void configure() {

	}
	
	@Override
	protected void postStart() {
		session = new MongoClientSession(configuration.getProperty("db.host", "localhost"), configuration.getPropertyAsInteger("db.port",27017),
						configuration.getProperty("db.username"), configuration.getProperty("db.password"),
						configuration.getPropertyAsInteger("db.maxConnections", 200), configuration.getProperty("db.database","exense"));
	}
	
	@Override
	public
	final void registerExplicitly(ResourceConfig resourceConfig) {
		//resourceConfig.registerClasses(SecurityFilter.class);
		registerExplicitly_(resourceConfig);
	}

	protected abstract void registerExplicitly_(ResourceConfig resourceConfig);
}
