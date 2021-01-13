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
package ch.exense.commons.core.server;

import org.glassfish.jersey.server.ResourceConfig;

import ch.exense.commons.core.web.container.DemoPublicServices;
import ch.exense.commons.core.web.container.ServerContext;

public class TestServer extends AbstractStandardServer {

	@Override
	public void postInitContext(ServerContext context) {
	}

	@Override
	public void initContext(ServerContext context) {
	}

	@Override
	public String provideWebappFolderName() {
		return null;
	}

	@Override
	public void registerExplicitly_(ResourceConfig resourceConfig) {
		// Not needed anymore due to registrable but leaving for tests of hard coded registration
		resourceConfig.registerClasses(DemoPublicServices.class);
	}

	@Override
	public String provideServiceContextPath() {
		return "/rest";
	}

	@Override
	protected void configure_() {
		// TODO Auto-generated method stub
		
	}

}
