package ch.exense.commons.core.server.security;


public interface AccessManager {

	public void setRoleResolver(RoleResolver roleResolver);
	
	public Role getRoleInContext(Session session);
	
	public boolean checkRightInContext(Session session, String right);
	
}
