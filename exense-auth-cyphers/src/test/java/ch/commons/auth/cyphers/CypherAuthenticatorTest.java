package ch.commons.auth.cyphers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.commons.auth.Credentials;

public class CypherAuthenticatorTest{

	private Credentials md5Credentials;
	private Credentials sshaCredentials;
	private CypherAuthenticator authenticator;

	@Before
	public void before() {
		this.md5Credentials = new Credentials();
		this.md5Credentials.setUsername("MD5User");
		this.md5Credentials.setPassword("abcd");

		this.sshaCredentials = new Credentials();
		this.sshaCredentials.setUsername("SSHAUser");
		this.sshaCredentials.setPassword("abcd");
		
		this.authenticator = new CypherAuthenticator(new MockedPasswordDirectory());
	}

	@Test
	public void authenticateSSHA() throws Exception {
		Assert.assertTrue(authenticator.authenticate(sshaCredentials));
	}

	@Test
	public void authenticateMD5() throws Exception {
		Assert.assertTrue(authenticator.authenticate(md5Credentials));
	}
}
