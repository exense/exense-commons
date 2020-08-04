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
	public void registerExplicitClasses(ResourceConfig resourceConfig) {
		resourceConfig.register(DemoServices.class);
	}

	@Override
	public String provideServiceContextPath() {
		return "/rest";
	}

}
