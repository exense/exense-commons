package ch.exense.commons.core.server;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.glassfish.jersey.server.ResourceConfig;

import ch.exense.commons.core.web.container.DemoServices;
import ch.exense.commons.core.web.container.ServerContext;

public class TestServer extends AutoconfigContainer {
	
	@Override
	public void initContext(ServerContext context) {
	}

	@Override
	public ContextHandler provideWebappContextHandler() {
		return null;
	}

	@Override
	public void registerPotentialClasses(ResourceConfig resourceConfig) {
		resourceConfig.register(DemoServices.class);
	}

	@Override
	public void postInitContext(ServerContext context) {
	}

	@Override
	public String provideServiceContextPath() {
		return "/rest";
	}

}
