package ch.exense.commons.core.access.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.commons.auth.cyphers.CypherAuthenticator;
import ch.commons.auth.ldap.LDAPClient;

public class LdapAuthenticator extends DirectoryComparisonAuthenticator{
	
	private static Logger logger = LoggerFactory.getLogger(LdapAuthenticator.class);

	private CypherAuthenticator authenticator;
	
	public LdapAuthenticator(LDAPClient directory) {	
		super(directory);
	}	
}
