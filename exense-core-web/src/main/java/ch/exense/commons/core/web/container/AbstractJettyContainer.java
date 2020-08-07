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

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.LogManager;

import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.exense.commons.app.ClasspathUtils;
import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.web.services.AbstractServices;

/**
 * This class is responsible for handling the core configuration and start of a Jetty based web container.
 * 
 * It implements the ExenseServer API in order to defer the start and configuration parsing mechanism to potential third party code.
 * If used in conjunction with the ServerStarter, mechanisms for handling configuration and finding the exact server implementation are provided. 
 * 
 * Concrete implementations based on this class define the actual services and concrete webapp to be deployed on the web container.
 */
public abstract class AbstractJettyContainer implements ExenseServer{

	private static final Logger logger = LoggerFactory.getLogger(AbstractJettyContainer.class);

	protected Configuration configuration;

	protected ServerContext context;
	
	protected ServiceRegistrationCallback serviceRegistrationCallback;

	private ContextHandlerCollection handlers;

	private ResourceConfig resourceConfig;

	private static Server jettyServer;

	private Integer port;

	@Override
	public void initialize(Configuration configuration) {
		this.configuration = configuration;
		this.port = configuration.getPropertyAsInteger("port", 8080);
		this.context = new ServerContext();
		context.setConfiguration(configuration);
		this.handlers = new ContextHandlerCollection();
		this.resourceConfig = new ResourceConfig();

		initialize_();
		
		// Concrete -- local responsibilities
		configureDefaults();
		
		// Abstract  -- call child server impl method
		configure_();
	}

	protected abstract void initialize_();

	private void configureDefaults() {
		
		setupLogging();
		
		setupRegistrationCallbacks();

		registerDefaultResources();

		registerPotentialServices();

		provideWebappContextHandler();

		// Abstract -- register specific child impl classes
		registerExplicitly(this.resourceConfig);
	}

	protected abstract void registerPotentialServices();

	protected abstract void configure_();

	@Override
	public void start() throws Exception {

		jettyServer = new Server();

		setupConnectors();
		
		addServletContainer();
		
		jettyServer.setHandler(handlers);
		jettyServer.start();
		
		postStart();
		
		jettyServer.join();	

	}

	protected abstract void postStart();

	private void registerDefaultResources() {

		resourceConfig.register(new AbstractBinder() {	
			@Override
			protected void configure() {
				bind(context).to(ServerContext.class);
				bind(configuration).to(Configuration.class);
				bindFactory(HttpSessionFactory.class).to(HttpSession.class)
				.proxy(true).proxyForSameScope(false).in(RequestScoped.class);
			}
		});
		
	}

	private synchronized void addHandler(Handler handler) {
		handlers.addHandler(handler);
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

	public void provideWebappContextHandler() {
		String providedWebappFolderName = provideWebappFolderName();

		// Using this convention for now for deploying webapp or not
		if(providedWebappFolderName != null) {
			ContextHandler webAppHandler = new ContextHandler("/");
			ResourceHandler bb = new ResourceHandler();
			bb.setResourceBase(Resource.newClassPathResource(providedWebappFolderName).getURI().toString());
			webAppHandler.setHandler(bb);
			addHandler(webAppHandler);
			logger.info("Will deploy webapp based on folder '"+providedWebappFolderName+"' on root context handler.");
		}else {
			logger.info("No webapp will be deployed.");
		}
	}

	public abstract String provideWebappFolderName();


	public abstract void registerExplicitly(ResourceConfig resourceConfig);

	public abstract String provideServiceContextPath();


	public void addServletContainer() {

		ServletContainer servletContainer = new ServletContainer(resourceConfig);

		ServletHolder sh = new ServletHolder(servletContainer);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(provideServiceContextPath());
		context.addServlet(sh, "/*");

		SessionHandler s = new SessionHandler();
		Integer timeout = configuration.getPropertyAsInteger("ui.sessiontimeout.minutes", 180)*60;
		s.setMaxInactiveInterval(timeout);
		s.setUsingCookies(true);
		s.setSessionCookie("sessionid");
		context.setSessionHandler(s);

		addHandler(context);
	}


	private void stop() {
		try {
			jettyServer.stop();
		} catch (Exception e) {
			logger.error("Error while stopping jetty",e);
		} finally {
			jettyServer.destroy();
		}
		if(configuration != null) {
			try {
				configuration.close();
			} catch (IOException e) {
				logger.error("Error while closing configuration",e);
			}
		}
	}

	private void setupConnectors() {
		HttpConfiguration http = new HttpConfiguration();
		http.addCustomizer(new SecureRequestCustomizer());
		http.setSecureScheme("https");

		ServerConnector connector = new ServerConnector(jettyServer);
		connector.addConnectionFactory(new HttpConnectionFactory(http));
		connector.setPort(port);

		if(configuration.getPropertyAsBoolean("ui.ssl.enabled", false)) {
			int httpsPort = configuration.getPropertyAsInteger("ui.ssl.port", 443);

			http.setSecurePort(httpsPort);

			HttpConfiguration https = new HttpConfiguration();
			https.addCustomizer(new SecureRequestCustomizer());

			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath(configuration.getProperty("ui.ssl.keystore.path"));
			sslContextFactory.setKeyStorePassword(configuration.getProperty("ui.ssl.keystore.password"));
			sslContextFactory.setKeyManagerPassword(configuration.getProperty("ui.ssl.keymanager.password"));

			ServerConnector sslConnector = new ServerConnector(jettyServer, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
			sslConnector.setPort(httpsPort);
			jettyServer.addConnector(sslConnector);
		}

		jettyServer.addConnector(connector);
	}

	private void setupRegistrationCallbacks() {

		try {
			init(new ServiceRegistrationCallback() {
				public void registerService(Class<?> serviceClass) {
					resourceConfig.registerClasses(serviceClass);
				}

				@Override
				public void registerHandler(Handler handler) {
					addHandler(handler);
				}

				@Override
				public void stop() {
					try {
						AbstractJettyContainer.this.stop();
					} catch (Exception e) {
						logger.error("Error while trying to stop the server",e);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected <T> void registerChildResources(Class<T> parentClass, String prefix) {
		for(Class<? extends T> c : ClasspathUtils.getSubTypesOf(parentClass, prefix)) {
			logger.info("Registering child resource of '"+parentClass+"' : '"+c+"'");
				resourceConfig.register(c);
		}		
	}

	public void destroy() {
		serviceRegistrationCallback.stop();
	}

	public ServerContext getContext() {
		return context;
	}

	protected static void setupLogging() {
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
	}
}