package ch.exense.commons.core.model.user;

public class ExternalUser extends User {
	
	public ExternalUser() {
		super();
		super.setRole("admin");
		super.setPreferences(new Preferences());
	}
}
