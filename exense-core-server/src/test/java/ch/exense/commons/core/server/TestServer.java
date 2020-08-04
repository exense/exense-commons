package ch.exense.commons.core.server;

import org.glassfish.jersey.server.ResourceConfig;

import ch.exense.commons.core.web.container.DemoServices;
import ch.exense.commons.core.web.container.ServerContext;

public class TestServer extends FullFeaturedServer {

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
		resourceConfig.registerClasses(DemoServices.class);
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
