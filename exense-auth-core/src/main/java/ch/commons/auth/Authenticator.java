package ch.commons.auth;

public interface Authenticator {
	public boolean authenticate(Credentials credentials) throws Exception;
}
