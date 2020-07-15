package ch.commons.auth.cyphers;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class StandardEncoder implements CypherEncoder{
	private String algorithm;

	public StandardEncoder(String algorithm) {
		this.algorithm = algorithm;
	}

	@Override
	public String encode(String password, String optionalPersistedHash, Charset charset) {
		MessageDigest md2 = null;
		try {
			md2 = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			// Realistically, we should never get in here..
			e.printStackTrace();
		}
		if(md2 != null) {
			return new String(Base64.getEncoder().encode(md2.digest(password.getBytes(charset))), charset);
		}else {
			return null;
		}
	}

}