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
