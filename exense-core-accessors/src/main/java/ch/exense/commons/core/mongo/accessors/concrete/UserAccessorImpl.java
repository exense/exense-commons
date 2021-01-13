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
package ch.exense.commons.core.mongo.accessors.concrete;

import java.util.ArrayList;
import java.util.List;

import ch.exense.commons.core.model.user.User;
import ch.exense.commons.core.model.user.UserAccessor;
import ch.exense.commons.core.mongo.MongoClientSession;
import ch.exense.commons.core.mongo.accessors.generic.AbstractCRUDAccessor;

public class UserAccessorImpl extends AbstractCRUDAccessor<User> implements UserAccessor {

	public UserAccessorImpl(MongoClientSession clientSession) {
		super(clientSession, "users", User.class);
	}

	@Override
	public void remove(String username) {
		collection.remove("{'username':'"+username+"'}");
	}
	
	@Override
	public List<User> getAllUsers() {
		List<User> result = new ArrayList<>();
		collection.find().as(User.class).iterator().forEachRemaining(u->result.add(u));
		return result;
	}
	
	@Override
	public User getByUsername(String username) {
		assert username != null;
		return collection.findOne("{username: #}", username).as(User.class);
	}
}
