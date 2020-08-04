package ch.exense.commons.core.server.security;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.commons.auth.Authenticator;
import ch.commons.auth.Credentials;
import ch.exense.commons.core.user.User;
import ch.exense.commons.core.user.UserAccessor;
import ch.exense.commons.core.web.container.ServerContext;
import ch.exense.commons.core.web.container.ServerContextAware;

public class DefaultAuthenticator implements Authenticator, ServerContextAware {
	
	private static Logger logger = LoggerFactory.getLogger(DefaultAuthenticator.class);
	
	private UserAccessor users;

	@Override
	public void setGlobalContext(ServerContext context) {
		users = (UserAccessor)context.get(UserAccessor.class.toString());
	}

	@Override
	public boolean authenticate(Credentials credentials) {
		String username = credentials.getUsername();
		String password = credentials.getPassword();
    	User user = users.getByUsername(username);
    	if(user!=null) {
			try {
				String pwdHash = DigestUtils.sha512Hex(password);				
				if(pwdHash.equals(user.getPassword())) {
					return true;
				} else {
					logger.debug("Password provided for '"+username+"' invalid.");
					return false;
				}
			} catch (Exception e) {
				logger.error("Error while trying to authenticate user '"+username+"'", e);
				return false;
			}
    	} else {
    		logger.debug("User '"+username+"' not found.");
    		return false;
    	} 	
	}
}