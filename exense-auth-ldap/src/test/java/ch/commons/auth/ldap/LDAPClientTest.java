package ch.commons.auth.ldap;

import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;

import ch.commons.auth.Credentials;
import ch.commons.auth.PasswordDirectory;
import ch.commons.auth.cyphers.CypherAuthenticator;

/* Utility E2E test class to test against a real LDAP client*/
public class LDAPClientTest {

	// Tech inputs for retrieving hashed password
	private final String ldapServer = "ldap.exense.ch";
	private final String ldapBaseDn = "dc=exense,dc=ch";
	private final String ldapUsername = "cn=admin,dc=exense,dc=ch";
	private final String ldapPassword = System.getProperty("adminPassword");
	
	private PasswordDirectory directory;
	private CypherAuthenticator authenticator;

	@Before
	public void before() throws NamingException {
		//ldap
		//directory = new LDAPClient("ldap://" + ldapServer + ":389",ldapBaseDn,ldapUsername,ldapPassword);
		//ldaps
		directory = new LDAPClient("ldaps://" + ldapServer + ":636",ldapBaseDn,ldapUsername,ldapPassword, "src/test/resources/ldap.jks", ldapPassword);
		authenticator = new CypherAuthenticator(directory);
	}
	
	@Test
	public void authenticateSSHA() throws Exception {
		Credentials credentials = new Credentials();
		credentials.setPassword("testpwdssha");
		credentials.setUsername("testpwdssha");

		authenticator.authenticate(credentials);
	}
	
	//@Test
	public void authenticateMD5() throws Exception {
		Credentials credentials = new Credentials();
		credentials.setPassword("testpwdmd5");
		credentials.setUsername("testpwdmd5");

		authenticator.authenticate(credentials);
	}
}