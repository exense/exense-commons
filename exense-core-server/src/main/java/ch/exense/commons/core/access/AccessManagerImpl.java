/*******************************************************************************
 * Copyright 2021 exense GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package ch.exense.commons.core.access;

import java.util.List;
import java.util.NoSuchElementException;

import ch.exense.commons.core.access.role.Role;
import ch.exense.commons.core.access.role.RoleProvider;
import ch.exense.commons.core.access.role.RoleResolver;
import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;
import ch.exense.commons.core.web.session.Session;

public class AccessManagerImpl implements AccessManager {

	private final RoleProvider roleProvider;
	private RoleResolver roleResolver;

	public AccessManagerImpl(RoleProvider roleProvider, RoleResolver roleResolver) {
		super();
		this.roleProvider = roleProvider;
		this.roleResolver = roleResolver;
	}

	@Override
	public void setRoleResolver(RoleResolver roleResolver) {
		this.roleResolver = roleResolver;
	}

	@Override
	public boolean checkRightInContext(Session session, String right) {
		Role role = getRoleInContext(session);
		return role.getRights().contains(right);
	}

	@Override
	public Role getRoleInContext(Session session) {
		String roleName = roleResolver.getRoleInContext(session);
		try {
			List<Role> roles = roleProvider.getRoles();
			Role role = roles.stream().filter(
								r->roleName.equals(r.getAttributes().get(AbstractOrganizableObject.NAME))
							).findFirst().get();
			return role;
		} catch (NoSuchElementException e) {
			throw new RuntimeException("The role "+roleName+" doesn't exist");
		}
	}
}
