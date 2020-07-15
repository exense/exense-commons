package ch.commons.auth;

public interface PasswordDirectory {
	public String getUserPassword(String username) throws Exception;
}