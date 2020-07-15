package ch.commons.auth.cyphers;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHAEncoder implements CypherEncoder{

	protected Logger logger = LoggerFactory.getLogger(SSHAEncoder.class);

	public SSHAEncoder() {
	}

	@Override
	public String encode(String password, String optionalPersistedHash, Charset charset) throws Exception {
		// trim now happening in the authenticator
		//String trimmed = optionalPersistedHash.substring("{SSHA}".length());

		logger.debug("as read from LDAP  = " + optionalPersistedHash);
		//logger.debug("trimmed            = " + trimmed);
		logger.debug("decoded            = " + Arrays.toString(Base64.getDecoder().decode(optionalPersistedHash)));
		logger.debug("      Length       = " + Base64.getDecoder().decode(optionalPersistedHash).length);
		logger.debug(" ------------------- ");

		byte[] decoded = Base64.getDecoder().decode(optionalPersistedHash);

		byte[] salt = new byte[4];
		salt[0] = decoded[20];
		salt[1] = decoded[21];
		salt[2] = decoded[22];
		salt[3] = decoded[23];

		logger.debug("              salt = "+ Arrays.toString(salt));
		logger.debug(" ------------------- ");		
		String encoded = generateSSHA(password.getBytes(charset), salt);

		String trimmed2 = encoded.substring("{SSHA}".length(), encoded.length());
		logger.debug("Password  provided = " + encoded);
		logger.debug("trimmed            = " + trimmed2);
		logger.debug("decoded            = " + Arrays.toString(Base64.getDecoder().decode(trimmed2)));
		logger.debug("      Length       = " + Base64.getDecoder().decode(trimmed2).length);

		logger.debug("      equals       = " + encoded.equals(optionalPersistedHash));
		
		return trimmed2;
	}
	
	public static String generateSSHA(byte[] password, byte[] salt)
			throws NoSuchAlgorithmException {

		MessageDigest crypt = MessageDigest.getInstance("SHA-1");
		crypt.reset();
		crypt.update(password);
		crypt.update(salt);
		byte[] hash = crypt.digest();

		byte[] hashPlusSalt = new byte[hash.length + salt.length];
		System.arraycopy(hash, 0, hashPlusSalt, 0, hash.length);
		System.arraycopy(salt, 0, hashPlusSalt, hash.length, salt.length);

		return new StringBuilder().append("{SSHA}")
				.append(Base64.getEncoder().encodeToString(hashPlusSalt))
				.toString();
	}

}