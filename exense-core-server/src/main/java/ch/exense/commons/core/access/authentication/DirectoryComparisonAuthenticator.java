package ch.exense.commons.core.access.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.commons.auth.Authenticator;
import ch.commons.auth.Credentials;
import ch.commons.auth.PasswordDirectory;
import ch.commons.auth.cyphers.CypherAuthenticator;

public class DirectoryComparisonAuthenticator implements Authenticator {
	
	private static Logger logger = LoggerFactory.getLogger(DirectoryComparisonAuthenticator.class);

	private CypherAuthenticator authenticator;
	
	public DirectoryComparisonAuthenticator(PasswordDirectory directory) {	
		this.authenticator = new CypherAuthenticator(directory);
	}	

	@Override
	public boolean authenticate(Credentials credentials) throws Exception {
		return authenticator.authenticate(credentials);
	}
}
