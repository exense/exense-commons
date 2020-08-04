package ch.exense.commons.core.access;

import ch.exense.commons.core.web.session.Session;

public interface AccessManager {

	public void setRoleResolver(RoleResolver roleResolver);
	
	public Role getRoleInContext(Session session);
	
	public boolean checkRightInContext(Session session, String right);
	
}
