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
package ch.exense.commons.core.web.server;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
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
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.util.concurrent.AbstractService;

import ch.exense.commons.app.ArgumentParser;
import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.web.container.GenericContainer;
import ch.exense.commons.core.web.container.HttpSessionFactory;
import ch.exense.commons.core.web.container.ServiceRegistrationCallback;
import ch.exense.commons.core.web.services.AbstractServices;


public class ServerStarter {

	public static Configuration configuration;

	private GenericContainer exenseServer;

	private static Server jettyServer;

	private ContextHandlerCollection handlers;

	private Integer port;

	private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);


	private synchronized void addHandler(Handler handler) {
		handlers.addHandler(handler);
	}

	public static void main(String[] args) throws Exception {
		ArgumentParser arguments = new ArgumentParser(args);

		String configStr = arguments.getOption("config");
		if(configStr!=null) {
			configuration = new Configuration(new File(configStr), arguments.getOptions());
		} else {
			configuration = new Configuration();
		}

		arguments.entrySet().forEach(e->configuration.putProperty(e.getKey(),e.getValue()));

		setupLogging();

		new ServerStarter().start();
	}

	protected static void setupLogging() {
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
	}

	public ServerStarter() {
		super();
	}

	public void start() throws Exception {
		this.port = configuration.getPropertyAsInteger("port", 8080);
		jettyServer = new Server();
		handlers = new ContextHandlerCollection();

		initServer();
		
		ContextHandler webapp = exenseServer.provideWebappContextHandler();
		if(webapp != null) {
			addHandler(webapp);
		}

		setupConnectors();

		jettyServer.setHandler(handlers);
		
		logger.info("Loaded Server type '" + exenseServer.getClass().getName() + "'");
		jettyServer.start();
		jettyServer.join();	
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

	private void initServer() throws Exception {

		exenseServer = serverFoundOnClassPath();
		
		ResourceConfig resourceConfig = new ResourceConfig();
		exenseServer.registerPotentialClasses(resourceConfig);


		exenseServer.init(new ServiceRegistrationCallback() {
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
					ServerStarter.this.stop();
				} catch (Exception e) {
					logger.error("Error while trying to stop the controller",e);
				}
			}
		});

		registerFoundServices(resourceConfig);

		resourceConfig.register(new AbstractBinder() {	
			@Override
			protected void configure() {
				bind(exenseServer).to(GenericContainer.class);
				bindFactory(HttpSessionFactory.class).to(HttpSession.class)
				.proxy(true).proxyForSameScope(false).in(RequestScoped.class);
			}
		});

		ServletContainer servletContainer = new ServletContainer(resourceConfig);

		ServletHolder sh = new ServletHolder(servletContainer);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(exenseServer.provideServiceContextPath());
		context.addServlet(sh, "/*");

		SessionHandler s = new SessionHandler();
		Integer timeout = configuration.getPropertyAsInteger("ui.sessiontimeout.minutes", 180)*60;
		s.setMaxInactiveInterval(timeout);
		s.setUsingCookies(true);
		s.setSessionCookie("sessionid");
		context.setSessionHandler(s);

		addHandler(context);
	}

	private void registerFoundServices(ResourceConfig resourceConfig) {
		for(Class c : getSubTypesOf(AbstractServices.class)) {
			resourceConfig.register(c);
		}
	}

	private GenericContainer serverFoundOnClassPath() {
		GenericContainer server = null;
		try {
			Set<Class<? extends GenericContainer>> serverClasses = getSubTypesOf(GenericContainer.class);
			
			for(Class clazz: serverClasses) {
				// Evaluate concrete types only
				 if(!Modifier.isAbstract( clazz.getModifiers())) {
					 // We found a concrete Server implementation, lets try to load its argument-less constructor
					 Constructor constructor = clazz.getConstructor();
					 if(constructor == null) {
						 // It doesn't have one: it must rely on Autoconfig
						 if(configuration == null) {
							 logger.info("Loading of configuration failed. Skipping starter '"+clazz.getName()+"'.");
							 continue;
						 }
						 constructor = AutoconfigContainer.class.getConstructor();
					 }else {
						 // It does have one: custom config case, nothing to do here, we'll just invoke that constructor
					 }
					 
					 server = (GenericContainer) constructor.newInstance();
					 break;
				 }
			}
			
			return server;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Could not load main ExenseServer class.");
	}

	private Set<Class<? extends GenericContainer>> getSubTypesOf(Class clazz) {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
			      .filterInputsBy(new FilterBuilder().includePackage("ch.exense"))
			      .setUrls(ClasspathHelper.forPackage("ch.exense"))
			      .setScanners(new SubTypesScanner()));
		return reflections.getSubTypesOf(clazz);
	}

	private Collection<Class> getServiceListFromClasspath() {
		ServiceLoader<AbstractService> loader = ServiceLoader.load(AbstractService.class);
		List<Class> classList = new ArrayList<>();
		for (AbstractService serv : loader) {
			classList.add(serv.getClass());
		}
		return classList;
	}

}
