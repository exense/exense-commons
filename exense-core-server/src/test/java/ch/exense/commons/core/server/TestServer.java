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
