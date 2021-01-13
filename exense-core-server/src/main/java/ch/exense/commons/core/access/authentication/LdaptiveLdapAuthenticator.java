/*******************************************************************************
 * Copyright 2021 exense GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package ch.exense.commons.core.access.authentication;

import ch.commons.auth.Authenticator;
import ch.commons.auth.Credentials;
import ch.exense.commons.app.Configuration;
import org.ldaptive.*;
import org.ldaptive.auth.*;
import org.ldaptive.control.ResponseControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdaptiveLdapAuthenticator implements Authenticator {

	private static Logger logger = LoggerFactory.getLogger(LdaptiveLdapAuthenticator.class);

    public LdaptiveLdapAuthenticator(Configuration configuration) {
    }

	public LdaptiveLdapAuthenticator() {
	}

	@Override
	public boolean authenticate(Credentials credentials) throws Exception {
		// use a secure connection for authentication
		ConnectionConfig connConfig = ConnectionConfig.builder()
				//.url("ldap://ldap.exense.ch")
				.url("ldap://159.100.252.110")
				.connectionInitializers(new BindConnectionInitializer("cn=admin,dc=exense,dc=ch", new Credential("100%LDAP")))
				.useStartTLS(false)
				.build();

		// use a search dn resolver
		SearchDnResolver dnResolver = SearchDnResolver.builder()
				.factory(new DefaultConnectionFactory(connConfig))
				.dn("dc=exense,dc=ch")
				.filter("(cn={user})")
				.build();

		SimpleBindAuthenticationHandler authHandler = new SimpleBindAuthenticationHandler(new DefaultConnectionFactory(connConfig));

		org.ldaptive.auth.Authenticator auth = new org.ldaptive.auth.Authenticator(dnResolver, authHandler);
		AuthenticationResponse response = auth.authenticate(
				new AuthenticationRequest(credentials.getUsername(), new Credential(credentials.getPassword()), null));
		if (response.isSuccess()) { // authentication succeeded
			LdapEntry entry = response.getLdapEntry();
			return true;
		} else { // authentication failed
			String msg = response.getDiagnosticMessage(); // read the failure message
			ResponseControl[] ctls = response.getControls(); // read any response controls
			return false;
		}
	}
}
