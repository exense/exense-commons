package ch.commons.auth.cyphers;

import java.nio.charset.Charset;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SHA512Encoder implements CypherEncoder{

	protected Logger logger = LoggerFactory.getLogger(SSHAEncoder.class);

	public SHA512Encoder() {
	}

	@Override
	public String encode(String password, String optionalPersistedHash, Charset charset) throws Exception {
		return DigestUtils.sha512Hex(password);
	}

}