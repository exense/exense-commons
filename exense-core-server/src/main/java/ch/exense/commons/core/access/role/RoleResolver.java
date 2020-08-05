package ch.exense.commons.core.access.role;

import ch.exense.commons.core.web.session.Session;

public interface RoleResolver {

	public String getRoleInContext(Session session);
}
