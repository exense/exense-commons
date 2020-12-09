package ch.exense.commons.core.access.authentication;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.commons.auth.Authenticator;
import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.model.user.UserAccessor;
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
			throw new AuthenticatorException("Unsupported authenticator type: " + authTypeProp);
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
			return new LdaptiveLdapAuthenticator(this.configuration);
		}

		// Actually unreachable.
		return null;
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