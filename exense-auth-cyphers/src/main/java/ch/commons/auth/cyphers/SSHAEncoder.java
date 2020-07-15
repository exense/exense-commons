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
		byte[] decoded = Base64.getDecoder().decode(optionalPersistedHash);
		
		logger.debug(" ------------------- ");
		logger.debug("        from LDAP  = " + optionalPersistedHash);
		logger.debug("decoded            = " + Arrays.toString(decoded));
		logger.debug("      Length       = " + Base64.getDecoder().decode(optionalPersistedHash).length);
		logger.debug(" ------------------- ");
		
		byte[] salt = Arrays.copyOfRange(decoded, 20, 24);
		byte[] encryptedNoSalt = Arrays.copyOf(decoded, 20);

		logger.debug("              salt = "+ Arrays.toString(salt));
		logger.debug("      encoded salt = "+ new String(Base64.getEncoder().encode(salt)), charset);
		logger.debug("   encryptedNoSalt = "+ Arrays.toString(encryptedNoSalt));
		logger.debug("   encryptedNoSalt = "+ new String(Base64.getEncoder().encode(encryptedNoSalt)), charset);
		logger.debug(" ------------------- ");		
		
		String encoded = generateSSHA(password, salt, charset);

		logger.debug("     user provided = " + encoded);
		logger.debug("decoded            = " + Arrays.toString(Base64.getDecoder().decode(encoded)));
		logger.debug("      Length       = " + Base64.getDecoder().decode(encoded).length);
		logger.debug(" ------------------- ");
		
		logger.debug("      equals       = " + encoded.equals(optionalPersistedHash));
		logger.debug(" ------------------- ");
		return encoded;
	}
	
	public static String generateSSHA(String password, byte[] salt, Charset charset)
			throws NoSuchAlgorithmException {

		MessageDigest crypt = MessageDigest.getInstance("SHA-1");
		crypt.reset();
		crypt.update(password.getBytes(charset));
		crypt.update(salt);
		byte[] hash = crypt.digest();

		byte[] hashPlusSalt = new byte[hash.length + salt.length];
		System.arraycopy(hash, 0, hashPlusSalt, 0, hash.length);
		System.arraycopy(salt, 0, hashPlusSalt, hash.length, salt.length);

		return new String(Base64.getEncoder().encode(hashPlusSalt), charset);
	}

}