package ch.exense.commons.core.server;

import org.eclipse.jetty.server.Handler;

public interface ServiceRegistrationCallback {
		
		public void registerService(Class<?> serviceClass);
		
		public void registerHandler(Handler handler);
		
		public void stop();
}
