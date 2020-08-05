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

import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import ch.exense.commons.app.ClasspathUtils;
import ch.exense.commons.core.access.AccessManager;
import ch.exense.commons.core.access.AccessManagerImpl;
import ch.exense.commons.core.access.AuthenticationManager;
import ch.exense.commons.core.access.DefaultAuthenticator;
import ch.exense.commons.core.access.DefaultRoleProvider;
import ch.exense.commons.core.access.RoleProvider;
import ch.exense.commons.core.access.RoleResolverImpl;
import ch.exense.commons.core.mongo.accessors.concrete.UserAccessorImpl;
import ch.exense.commons.core.mongo.accessors.generic.MongoClientSession;
import ch.exense.commons.core.user.User;
import ch.exense.commons.core.user.UserAccessor;
import ch.exense.commons.core.web.container.AbstractJettyContainer;
import ch.exense.commons.core.web.container.JacksonMapperProvider;
import ch.exense.commons.core.web.services.AbstractServices;

/**
 * This class builds upon the abstract container to provide the following additional services:
 * - done: automatic handling and creation of the mongo db session based on config object and variables convention
 * - done: automatic registration of AbstractServices
 * - done: automatic registration of web services implementing Registrable
 * - in progress: automatic registration of user management services
 * - in progress: automatic registration of login service, security filter
 * - in progress: automatic registration of mongo + ldap authenticators
 * - not yet implemented: automatic registration of mongo + ldap role managers 
 * - not yet implemented: automatic binding of (mongodb) accessors  
 */

public abstract class AbstractStandardServer extends AbstractJettyContainer{

	protected MongoClientSession session;

	private AuthenticationManager authenticationManager;

	private AccessManager accessManager;

	private UserAccessor userAccessor;
	
	private RoleProvider roleProvider;

	private static final Logger logger = LoggerFactory.getLogger(AbstractStandardServer.class);

	public AbstractStandardServer() {
		super();
	}

	@Override
	final protected void initialize_() {
		session = new MongoClientSession(configuration.getProperty("db.host", "localhost"), configuration.getPropertyAsInteger("db.port",27017),
				configuration.getProperty("db.username"), configuration.getProperty("db.password"),
				configuration.getPropertyAsInteger("db.maxConnections", 200), configuration.getProperty("db.database","exense"));

		//TODO: automatically instantiate all Accessors with session object and bind them for injection
		userAccessor = new UserAccessorImpl(session);
		super.context.put(UserAccessorImpl.class.getName(), userAccessor);

		initAdminIfNecessary();
		
		authenticationManager = new AuthenticationManager(configuration, new DefaultAuthenticator(userAccessor), userAccessor);

		roleProvider = new DefaultRoleProvider();
		
		accessManager = new AccessManagerImpl(roleProvider, new RoleResolverImpl(userAccessor));
	}

	private void initAdminIfNecessary() {
		if(userAccessor.getByUsername("admin") == null) {
			User admin = new User();
			admin.setUsername("admin");
			admin.setPassword(DigestUtils.sha512Hex("init"));
			admin.setRole("admin");
			userAccessor.save(admin);
		}
	}

	@Override
	protected void postStart() {

	}

	@Override
	protected void registerPotentialServices() {
		String packageList = configuration.getProperty("ch.exense.core.services.packagePrefix", "ch.exense");

		logger.info("Registering AbstractServices.");
		//Not needed?
		//resourceConfig.register(AbstractServices.class);

		// Allow multiple prefixes via configuration
		for(String prefix : Arrays.asList(packageList.split(";"))) {
			registerChildResources(AbstractServices.class, prefix);
		}
	}	


	@Override
	public final void registerExplicitly(ResourceConfig resourceConfig) {
		// automatic service registration
		registerRegistrables(resourceConfig);

		// commonly used mappers
		resourceConfig.register(JacksonMapperProvider.class);
		resourceConfig.register(JacksonJaxbJsonProvider.class);

		// user management & security
		resourceConfig.register(new AbstractBinder() {	
			@Override
			protected void configure() {
				//TODO: automatically instantiate all Accessors with session object and bind them for injection
				bind(userAccessor).to(UserAccessor.class);

				bind(roleProvider).to(RoleProvider.class);
				bind(authenticationManager).to(AuthenticationManager.class);
				bind(accessManager).to(AccessManager.class);
			}
		});

		// abstract call, opportunity to expand for children classes
		registerExplicitly_(resourceConfig);
	}

	private void registerRegistrables(ResourceConfig resourceConfig) {
		for (Class<? extends Registrable> r: ClasspathUtils.getAllConcreteSubTypesOf(Registrable.class, "ch.exense")) {
			logger.info("Registering Registrable class '"+r+"'");
			resourceConfig.registerClasses(r);
		}
	}

	protected abstract void registerExplicitly_(ResourceConfig resourceConfig);
}
