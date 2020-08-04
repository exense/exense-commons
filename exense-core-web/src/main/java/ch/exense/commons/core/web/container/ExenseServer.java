package ch.exense.commons.core.web.container;

import ch.exense.commons.app.Configuration;

public interface ExenseServer {

	public void initialize(Configuration configuration);
	public void start() throws Exception;
}
