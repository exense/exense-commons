package ch.exense.commons.core.access;

import ch.exense.commons.core.web.session.Session;

public interface RoleResolver {

	public String getRoleInContext(Session session);
}
