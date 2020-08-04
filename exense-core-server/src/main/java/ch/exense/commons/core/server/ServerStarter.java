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

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.exense.commons.app.ArgumentParser;
import ch.exense.commons.app.ClasspathUtils;
import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.web.container.ExenseServer;

/**
 * This is the main starter class providing configuration parsing and search for a concrete server to start on the class path
 */
public class ServerStarter {

	public static Configuration configuration;

	private ExenseServer exenseServer;

	private static final Logger logger = LoggerFactory.getLogger(ServerStarter.class);

	public static void main(String[] args) throws Exception {
		ArgumentParser arguments = new ArgumentParser(args);

		String configStr = arguments.getOption("config");
		if(configStr!=null) {
			configuration = new Configuration(new File(configStr), arguments.getOptions());
		} else {
			configuration = new Configuration();
		}

		arguments.entrySet().forEach(e->configuration.putProperty(e.getKey(),e.getValue()));

		new ServerStarter().start();
	}

	public ServerStarter() {
		super();
	}

	public void start() throws Exception {
		exenseServer = serverFoundOnClassPath();
		logger.info("Loaded Server type '" + exenseServer.getClass().getName() + "'");
		exenseServer.initialize(configuration);
		exenseServer.start();
	}

	@SuppressWarnings("rawtypes")
	private ExenseServer serverFoundOnClassPath() {
		ExenseServer server = null;
		try {
			String packagePrefix = configuration.getProperty("ch.exense.core.starter.packagePrefix", "ch.exense");
			Set<Class> serverClasses = ClasspathUtils.getAllConcreteSubTypesOf(ExenseServer.class, packagePrefix);

			for(Class clazz: serverClasses) {
				Constructor constructor = clazz.getConstructor();
				if(constructor != null) {
					return (ExenseServer) constructor.newInstance();
				}else {
					throw new RuntimeException("Could not find argument-less constructor for server type '"+clazz+"'.");
				}
			}

			throw new RuntimeException("No suitable/concrete container implementation type found to launch jetty with (must concretely extend "+ExenseServer.class+".");

		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Could not load main ExenseServer class.");
	}

}
