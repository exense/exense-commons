package ch.commons.auth.cyphers;

import java.nio.charset.Charset;

public interface CypherEncoder {
	String encode(String password, String optionalPersistedHash, Charset charset) throws Exception;
}
