package ch.exense.commons.core.server.security;

import ch.exense.commons.core.access.User;

public class ExternalUser extends User {
	
	public ExternalUser() {
		super();
		super.setRole("admin");
		super.setPreferences(new Preferences());
	}
}
