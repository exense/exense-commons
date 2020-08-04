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

import ch.exense.commons.app.Configuration;

public class ServerContext extends AbstractContext {

	private Configuration configuration; 

	private ServiceRegistrationCallback serviceRegistrationCallback;

	public ServerContext() {
		super();
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public ServiceRegistrationCallback getServiceRegistrationCallback() {
		return serviceRegistrationCallback;
	}

	public void setServiceRegistrationCallback(ServiceRegistrationCallback serviceRegistrationCallback) {
		this.serviceRegistrationCallback = serviceRegistrationCallback;
	}
}