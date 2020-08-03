package ch.exense.commons.core.server;

import ch.exense.commons.app.Configuration;

public class TestServer extends ExenseServer {

	public TestServer() {
		//TODO: getconf
		super(new Configuration());

	}

	@Override
	protected void registerStuff() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initContext(ServerContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void sayHi() {
		System.out.println(" ----- HELLO WORLD -------------");
	}

}
