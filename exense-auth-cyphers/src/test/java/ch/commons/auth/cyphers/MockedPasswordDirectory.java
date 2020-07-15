package ch.commons.auth.cyphers;

import ch.commons.auth.PasswordDirectory;

public class MockedPasswordDirectory implements PasswordDirectory{

	@Override
	public String getUserPassword(String username) throws Exception {
		if(username.contains("MD5")) {
			return "{MD5}4vxxTEcn7pOV8yTNLn8zHw==";
		}
		
		if(username.contains("SSHA")) {
			return "{SSHA}W89tolUok4+Nc9vV/xmiVPRSUl/Fu4G2";
		}
		
		if(username.contains("ABS")) {
			return null;
		}

		throw new Exception("This mock requires the corresponding cypher to be placed in the user name.");
	}

}
