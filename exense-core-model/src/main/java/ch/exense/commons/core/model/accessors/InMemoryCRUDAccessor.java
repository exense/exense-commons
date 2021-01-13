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
package ch.exense.commons.core.model.accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.types.ObjectId;

public class InMemoryCRUDAccessor<T extends AbstractIdentifiableObject> implements CRUDAccessor<T> {

	protected Map<ObjectId, T> map = new ConcurrentHashMap<>();

	@Override
	public T get(ObjectId id) {
		return map.get(id);
	}

	@Override
	public T get(String id) {
		return get(new ObjectId(id));
	}

	@Override
	public T findByAttributes(Map<String, String> attributes) {
		return map.values().stream().filter(v->{
			if(v instanceof AbstractOrganizableObject) {
				return ((AbstractOrganizableObject)v).attributes.equals(attributes);
			} else {
				if(v instanceof AbstractIdentifiableObject) {
					return ((AbstractIdentifiableObject)v).customFields.equals(attributes);
				}else {
					return false;
				}
			}
		}).findFirst().orElse(null);
	}

	@Override
	public Spliterator<T> findManyByAttributes(Map<String, String> attributes) {
		return map.values().stream().filter(v->{
			if(v instanceof AbstractOrganizableObject) {
				return ((AbstractOrganizableObject)v).attributes.equals(attributes);
			} else {
				if(v instanceof AbstractIdentifiableObject) {
					return ((AbstractIdentifiableObject)v).customFields.equals(attributes);
				}else {
					return false;
				}
			}
		}).spliterator();
	}

	@Override
	public Iterator<T> getAll() {
		return map.values().iterator();
	}

	@Override
	public void remove(ObjectId id) {
		map.remove(id);
	}

	@Override
	public T save(T entity) {
		if(entity.getId()==null) {
			entity.setId(new ObjectId());
		}
		map.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public void save(Collection<? extends T> entities) {
		entities.forEach(e->save(e));
	}

	@Override
	public T findByAttributes(Map<String, String> attributes, String attributesMapKey) {
		return map.values().stream().filter(v->{
			if(attributesMapKey.equals("attributes")) {
				return ((AbstractOrganizableObject)v).attributes.equals(attributes);
			} else {
				if(attributesMapKey.equals("customFields")) {
					return ((AbstractIdentifiableObject)v).customFields.equals(attributes);
				}else {
					return false;
				}
			}
		}).findFirst().orElse(null);
	}

	@Override
	public Spliterator<T> findManyByAttributes(Map<String, String> attributes, String attributesMapKey) {
		return map.values().stream().filter(v->{
			if(v instanceof AbstractOrganizableObject) {
				return ((AbstractOrganizableObject)v).attributes.equals(attributes);
			} else {
				if(attributesMapKey.equals("customFields")) {
					return ((AbstractIdentifiableObject)v).customFields.equals(attributes);
				}else {
					return false;
				}
			}
		}).spliterator();
	}

	@Override
	public List<T> getRange(int skip, int limit) {
		List<T> list = new ArrayList<>(map.values());
		if(skip<list.size()) {
			return list.subList(skip, Math.min(list.size(), skip+limit));
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public Collection<T> getAllAsCollection() {
		return map.values();
	}
}
