package ch.commons.auth.ldap;

import org.junit.Test;

public class PAC4JClientTest {

	public PAC4JClientTest() {
		super();
		// TODO Auto-generated constructor stub
	}

	//@Test
	public void testPac() {
		PAC4JClient client = new PAC4JClient();
		System.out.println(client.getDn());
		client.validate();
	}

}
