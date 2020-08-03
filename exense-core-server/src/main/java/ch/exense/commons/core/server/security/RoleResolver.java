package ch.exense.commons.core.server.security;

public interface RoleResolver {

	public String getRoleInContext(Session session);
}
