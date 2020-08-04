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

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import ch.exense.commons.app.ClasspathUtils;
import ch.exense.commons.core.mongo.accessors.generic.MongoClientSession;
import ch.exense.commons.core.web.container.AbstractJettyContainer;
import ch.exense.commons.core.web.container.JacksonMapperProvider;

/**
 * This class builds upon the abstract container to provide additional services such as
 * - automatic handling and creation of the mongo db session
 * - automatic registration of web services implementing Registrable
 * - not yet implemented: automatic binding of (mongodb) accessors  
 */

public abstract class FullFeaturedServer extends AbstractJettyContainer{

	protected MongoClientSession session;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(FullFeaturedServer.class);
	
	public FullFeaturedServer() {
		super();
	}
	
	@Override
	final protected void initialize_() {
		session = new MongoClientSession(configuration.getProperty("db.host", "localhost"), configuration.getPropertyAsInteger("db.port",27017),
				configuration.getProperty("db.username"), configuration.getProperty("db.password"),
				configuration.getPropertyAsInteger("db.maxConnections", 200), configuration.getProperty("db.database","exense"));
		/*
		 * TODO: automatically instantiate all Accessors with session object and bind them for injection
		 */
		//bindBindables(resourceConfig);
	}
	
	@Override
	protected void postStart() {

	}
	
	@Override
	public final void registerExplicitly(ResourceConfig resourceConfig) {
		registerRegistrables(resourceConfig);
		resourceConfig.register(JacksonMapperProvider.class);
		resourceConfig.register(JacksonJaxbJsonProvider.class);
		registerExplicitly_(resourceConfig);
	}

	private void registerRegistrables(ResourceConfig resourceConfig) {
		for (Class<? extends Registrable> r: ClasspathUtils.getAllConcreteSubTypesOf(Registrable.class, "ch.exense")) {
			resourceConfig.registerClasses(r);
		}
	}

	protected abstract void registerExplicitly_(ResourceConfig resourceConfig);
}
