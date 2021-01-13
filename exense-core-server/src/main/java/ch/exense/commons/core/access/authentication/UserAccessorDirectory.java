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
package ch.exense.commons.core.access.authentication;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

import org.bson.types.ObjectId;

import ch.exense.commons.core.model.user.User;
import ch.exense.commons.core.model.user.UserAccessor;

public class UserAccessorDirectory implements UserAccessorDirectoryI{
	
	private UserAccessor userAccessor;
	
	public UserAccessorDirectory(UserAccessor userAccessor) {
		this.userAccessor = userAccessor;
	}

	@Override
	public void remove(String username) {
		userAccessor.remove(username);
	}

	@Override
	public List<User> getAllUsers() {
		return userAccessor.getAllUsers();
	}

	@Override
	public User getByUsername(String username) {
		return userAccessor.getByUsername(username);
	}

	@Override
	public void remove(ObjectId id) {
		userAccessor.remove(id);
	}

	@Override
	public User save(User entity) {
		return userAccessor.save(entity);
	}

	@Override
	public void save(Collection<? extends User> entities) {
		userAccessor.save(entities);
	}

	@Override
	public User get(ObjectId id) {
		return userAccessor.get(id);
	}

	@Override
	public User get(String id) {
		return userAccessor.get(id);
	}

	@Override
	public User findByAttributes(Map<String, String> attributes) {
		return userAccessor.findByAttributes(attributes);
	}

	@Override
	public Spliterator<User> findManyByAttributes(Map<String, String> attributes) {
		return userAccessor.findManyByAttributes(attributes);
	}

	@Override
	public Iterator<User> getAll() {
		return userAccessor.getAll();
	}

	@Override
	public User findByAttributes(Map<String, String> attributes, String attributesMapKey) {
		return userAccessor.findByAttributes(attributes, attributesMapKey);
	}

	@Override
	public Spliterator<User> findManyByAttributes(Map<String, String> attributes, String attributesMapKey) {
		return userAccessor.findManyByAttributes(attributes, attributesMapKey);
	}

	@Override
	public List<User> getRange(int skip, int limit) {
		return userAccessor.getRange(skip, limit);
	}

	@Override
	public String getUserPassword(String username) throws Exception {
		return userAccessor.getByUsername(username).getPassword();
	}

	@Override
	public Collection<User> getAllAsCollection() {
		return userAccessor.getAllAsCollection();
	}

}
