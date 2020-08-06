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

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import ch.commons.auth.Authenticator;
import ch.commons.auth.cyphers.SupportedCypher;
import ch.exense.commons.app.ClasspathUtils;
import ch.exense.commons.core.access.AccessManager;
import ch.exense.commons.core.access.AccessManagerImpl;
import ch.exense.commons.core.access.authentication.AuthenticationManager;
import ch.exense.commons.core.access.authentication.AuthenticatorFactory;
import ch.exense.commons.core.access.authentication.AuthenticatorFactory.AuthenticatorException;
import ch.exense.commons.core.access.role.DefaultRoleProvider;
import ch.exense.commons.core.access.role.RoleProvider;
import ch.exense.commons.core.access.role.RoleResolverImpl;
import ch.exense.commons.core.model.user.User;
import ch.exense.commons.core.model.user.UserAccessor;
import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.concrete.UserAccessorImpl;
import ch.exense.commons.core.web.container.AbstractJettyContainer;
import ch.exense.commons.core.web.container.JacksonMapperProvider;
import ch.exense.commons.core.web.services.AbstractServices;

/**
 * This class builds upon the abstract container to provide the following additional services:
 * automatic handling and creation of the mongo db session based on config object and variables convention
 * automatic registration of AbstractServices
 * automatic registration of web services implementing Registrable
 * automatic registration of default user and role management services
 * automatic registration of login service, security filter (mongo impl)
 * (in progress) support of ldap auth (and config-based authenticator factory)
 * (in progress) support of generic role mgmt model 
 * (in progress) automatic binding of (mongodb) accessors
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
		super.context.put(UserAccessor.class.getName(), userAccessor);

		//temporary until initialization component supported
		initAdminIfNecessary();
		
		initializeAuthentication();

		roleProvider = new DefaultRoleProvider();
		
		accessManager = new AccessManagerImpl(roleProvider, new RoleResolverImpl(userAccessor));
	}

	private void initializeAuthentication() {
		Authenticator authenticator;
		try {
			authenticator = new AuthenticatorFactory(super.configuration, super.context).getAuthenticator();
		} catch (AuthenticatorException e) {
			e.printStackTrace();
			throw new RuntimeException("A critical exception has occured, server initialization failed.", e);
		}
		
		authenticationManager = new AuthenticationManager(configuration, authenticator, userAccessor);
	}

	private void initAdminIfNecessary() {
		if(userAccessor.getByUsername("admin") == null) {
			User admin = new User();
			admin.setUsername("admin");
			try {
				admin.setPassword(SupportedCypher.SHA512.encoder.encode("init", null, null));
			} catch (Exception e) {
				e.printStackTrace();
			}
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

	//TODO: remove Registrable interface and register classes marked as @Singleton instead?
	private void registerRegistrables(ResourceConfig resourceConfig) {
		String packagePrefix = configuration.getProperty("ch.exense.core.registrable.packagePrefix", "ch.exense");
		for (Class<? extends Registrable> r: ClasspathUtils.getAllConcreteSubTypesOf(Registrable.class, packagePrefix)) {
			logger.info("Registering Registrable class '"+r+"'");
			resourceConfig.registerClasses(r);
		}
	}

	protected abstract void registerExplicitly_(ResourceConfig resourceConfig);
}
