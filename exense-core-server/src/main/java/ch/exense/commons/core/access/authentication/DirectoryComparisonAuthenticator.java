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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.commons.auth.Authenticator;
import ch.commons.auth.Credentials;
import ch.commons.auth.PasswordDirectory;

public class DirectoryComparisonAuthenticator implements Authenticator {

    protected Logger logger = LoggerFactory.getLogger(DirectoryComparisonAuthenticator.class);

    private String cypher = null;
    private PasswordDirectory directory;

    public DirectoryComparisonAuthenticator(PasswordDirectory directory) {
        this.directory = directory;
    }

    @Override
    public boolean authenticate(Credentials credentials) throws Exception {
        String encodedPassword = directory.getUserPassword(credentials.getUsername());

        if (encodedPassword == null) {
            throw new Exception("User '" + credentials.getUsername() + "' could not be found in directory.");
        }

        if (cypher == null || cypher.isEmpty()) {
            // Attempt to infer cypher from prefix of encoded password
            if (!(encodedPassword.contains("{") && encodedPassword.contains("}"))) {
                /**
                 * Temporarily defaulting to SHA512 for implicit compatibility with legacy step implementation
                 */
                //throw new Exception("Cypher could not be implcitly inferred from password hash. Please set CypherEncoder explicitly.");
                cypher = "SHA512";
            } else {
                cypher = encodedPassword.split("\\{")[1].split("\\}")[0];
            }
        }

        /**
         * Temporarily handling special case for legacy step implementation
         */
        String persisted = "SHA512".equals(cypher) ? encodedPassword : encodedPassword.substring(cypher.length() + 2);
        String provided = AuthenticationManager.hashPassword(credentials.getPassword());
        boolean authResult = persisted.equals(provided);

        logger.debug(persisted + "==" + provided + " ? " + authResult);

        return authResult;
    }
}
