package ch.exense.commons.core.server;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.glassfish.jersey.server.ResourceConfig;

public class TestServer extends AutoconfigServer {
	
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

}
