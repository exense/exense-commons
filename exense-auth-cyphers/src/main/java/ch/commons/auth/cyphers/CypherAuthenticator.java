package ch.commons.auth.cyphers;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.commons.auth.Authenticator;
import ch.commons.auth.Credentials;
import ch.commons.auth.PasswordDirectory;

public class CypherAuthenticator implements Authenticator{

	protected Logger logger = LoggerFactory.getLogger(CypherAuthenticator.class);

	private String cypher = null;
	private PasswordDirectory directory;
	private Charset charset;

	public CypherAuthenticator(String cypher, PasswordDirectory directory, Charset charset) {
		this.cypher = cypher;
		this.directory = directory;
		this.charset = charset;
	}

	public CypherAuthenticator(PasswordDirectory directory) {
		this.directory = directory;
		this.charset = Charset.forName("UTF-8");
	}

	@Override
	public boolean authenticate(Credentials credentials) throws Exception {
		String encodedPassword = directory.getUserPassword(credentials.getUsername());
		
		if(encodedPassword == null) {
			throw new Exception("User '"+credentials.getUsername()+"' could not be found in directory.");
		}

		if(cypher == null || cypher.isEmpty()) {
			// Attempt to infer cypher from prefix of encoded password
			if(! (encodedPassword.contains("{") && encodedPassword.contains("}"))){
				/**
				 * Temporarily defaulting to SHA512 for implicit compatibility with legacy step implementation
				 */
				//throw new Exception("Cypher could not be implcitly inferred from password hash. Please set CypherEncoder explicitly.");
				cypher = "SHA512";
			}else {
				cypher = encodedPassword.split("\\{")[1].split("\\}")[0];
			}
		}

		SupportedCypher supportedCypher = SupportedCypher.forName(cypher);
		
		/**
		 * Temporarily handling special case for legacy step implementation
		 */
		String persisted = "SHA512".equals(cypher) ? encodedPassword : encodedPassword.substring(cypher.length() + 2);
		String provided = supportedCypher.encoder.encode(credentials.getPassword(), persisted, this.charset);
		boolean authResult = persisted.equals(provided);
		
		logger.debug(persisted + "==" + provided + " ? " + authResult);
		
		return authResult;
	}
}
