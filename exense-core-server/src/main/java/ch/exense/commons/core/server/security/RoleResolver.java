package ch.exense.commons.core.server.security;

import ch.exense.commons.core.web.session.Session;

public interface RoleResolver {

	public String getRoleInContext(Session session);
}
