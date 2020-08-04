package ch.exense.commons.core.server;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.glassfish.jersey.server.ResourceConfig;

import ch.exense.commons.core.web.container.AutoconfigContainer;
import ch.exense.commons.core.web.container.DemoServices;
import ch.exense.commons.core.web.container.ServerContext;

public class TestServer extends AutoconfigContainer {
	
	@Override
	protected void initContext(ServerContext context) {
	}

	@Override
	protected ContextHandler provideWebappContextHandler() {
		return null;
	}

	@Override
	protected void registerPotentialClasses(ResourceConfig resourceConfig) {
		resourceConfig.register(DemoServices.class);
	}

	@Override
	protected void postInitContext(ServerContext context) {
	}

	@Override
	protected String provideServiceContextPath() {
		return "/rest";
	}

}
