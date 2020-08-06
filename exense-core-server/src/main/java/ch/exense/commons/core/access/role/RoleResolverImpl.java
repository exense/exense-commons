package ch.exense.commons.core.access.role;

import ch.exense.commons.core.model.user.ExternalUser;
import ch.exense.commons.core.model.user.User;
import ch.exense.commons.core.model.user.UserAccessor;
import ch.exense.commons.core.web.session.Session;

public class RoleResolverImpl implements RoleResolver {

	private final UserAccessor userAccessor;
	
	public RoleResolverImpl(UserAccessor userAccessor) {
		super();
		this.userAccessor = userAccessor;
	}

	@Override
	public String getRoleInContext(Session session) {
		User user = userAccessor.get(session.getUser().getId());
		
		//For compatibility with external user management
		if(user == null) {
			user = new ExternalUser();
			user.setUsername(session.getUser().getUsername());
		}
		
		return user.getRole();
	}

}
