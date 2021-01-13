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
package ch.exense.commons.core.access.role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.exense.commons.core.model.accessors.AbstractOrganizableObject;

public class DefaultRoleProvider implements RoleProvider {

	public static Role DEFAULT_ROLE;
	private static List<Role> DEFAULT_ROLES = new ArrayList<Role>();

	{
		DEFAULT_ROLE = new Role();
		DEFAULT_ROLE.addAttribute(AbstractOrganizableObject.NAME, "admin");
		DEFAULT_ROLE.setRights(Arrays.asList(new String[]{"interactive","plan-read","plan-write","plan-delete","plan-execute","kw-read","kw-write","kw-delete","kw-execute","execution-read","execution-write","execution-delete","user-write","user-read","task-read","task-write","task-delete","admin","param-read","param-write","param-delete","param-global-write","token-manage"}));
		
		DEFAULT_ROLES.add(DEFAULT_ROLE);
	}

	public DefaultRoleProvider() {
		super();
	}

	@Override
	public List<Role> getRoles() {
		return DEFAULT_ROLES;
	}
}
