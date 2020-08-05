package ch.exense.commons.core.access.authentication;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.commons.auth.Authenticator;
import ch.commons.auth.ldap.LDAPClient;
import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.user.UserAccessor;
import ch.exense.commons.core.web.container.ServerContext;

public class AuthenticatorFactory {

	private ServerContext context;

	private Configuration configuration;

	private static final Logger logger = LoggerFactory.getLogger(AuthenticatorFactory.class);

	public AuthenticatorFactory(Configuration configuration, ServerContext context) {
		this.context = context;	
		this.configuration = configuration;
	}

	public Authenticator getAuthenticator() throws AuthenticatorException {

		String authTypeProp = configuration.getProperty("ui.authenticator.type", "accessor");
		SupportedAuthenticators authenticatorType = SupportedAuthenticators.forName(authTypeProp);

		if(authenticatorType == null) {
		}

		switch(authenticatorType){
		case ACCESSOR:
			/**
			 * Mongo is currently the only supported backend but an AccessorFactory may have to be involved here in the future
			 * in order to support UserAccessor-based authenticators which are backed by other backend types.
			 * The exact implementation would then have to be derived from the server context / config.
			 * 
			 * We're also assuming that the accessor has already been set in context at an earlier stage.
			 * 
			 */
			return new MongoAuthenticator((UserAccessor)this.context.get(UserAccessor.class));
		case LDAP:
			/**
			 * This would shift the problem of doing ldap-specific stuff outside of the factory which isn't nice
			 * Instead, we're making the factory configuration-aware and creating the LDAPClient in here.
			 */
			//return new LdapAuthenticator((LDAPClient)this.context.get(LDAPClient.class));

			try {
				return new LdapAuthenticator(buildLDAPClientFromConfig(this.configuration));
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}

		// Actually unreachable.
		return null;
	}


	private static LDAPClient buildLDAPClientFromConfig(Configuration configuration) throws NamingException {
		String ldapUrl = configuration.getProperty("ui.authenticator.ldap.url",null);
		String ldapBaseDn = configuration.getProperty("ui.authenticator.ldap.base",null);
		String ldapTechuser = configuration.getProperty("ui.authenticator.ldap.techuser",null);
		String ldapTechpwd = configuration.getProperty("ui.authenticator.ldap.techpwd",null);

		// Ldap over SSL case
		String pathToJks = configuration.getProperty("ui.authenticator.ldap.ssl.pathToJks",null);
		String jksPassword = configuration.getProperty("ui.authenticator.ldap.ssl.jksPassword",null);

		return new LDAPClient(ldapUrl,ldapBaseDn,ldapTechuser,ldapTechpwd, pathToJks, jksPassword);
	}


	public enum SupportedAuthenticators {
		ACCESSOR("accessor"),
		LDAP("ldap");

		private final String name;

		SupportedAuthenticators(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static SupportedAuthenticators forName(String authType) {
			switch(authType){
			case "accessor":
				return SupportedAuthenticators.ACCESSOR;
			case "ldap":
				return SupportedAuthenticators.LDAP;
			}
			return null;
		}
	}


	@SuppressWarnings("serial")
	public class AuthenticatorException extends Exception{
		AuthenticatorException(String message){
			super(message);
		}

		AuthenticatorException(String message, Throwable e){
			super(message, e);
		}
	}

}