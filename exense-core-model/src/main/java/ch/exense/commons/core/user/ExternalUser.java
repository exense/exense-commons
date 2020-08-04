package ch.exense.commons.core.user;

public class ExternalUser extends User {
	
	public ExternalUser() {
		super();
		super.setRole("admin");
		super.setPreferences(new Preferences());
	}
}
