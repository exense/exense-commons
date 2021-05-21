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
package ch.exense.commons.core.accessors;

import ch.exense.commons.core.collections.Collection;
import ch.exense.commons.core.collections.Filter;
import ch.exense.commons.core.collections.Filters;
import ch.exense.commons.core.model.accessors.AbstractIdentifiableObject;
import org.bson.types.ObjectId;


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbstractAccessor<T extends AbstractIdentifiableObject> implements Accessor<T> {

	protected final Collection<T> collectionDriver;

	public AbstractAccessor(Collection<T> collectionDriver) {
		super();
		this.collectionDriver = collectionDriver;
	}

	public Collection<T> getCollectionDriver() {
		return collectionDriver;
	}

	@Override
	public T get(ObjectId id) {
		return collectionDriver.find(byId(id), null, null, null, 0).findFirst().orElse(null);
	}

	@Override
	public T get(String id) {
		return get(new ObjectId(id));
	}

	@Override
	public T findByAttributes(Map<String, String> attributes) {
		return findByKeyAttributes("attributes", attributes);
	}

	@Override
	public Spliterator<T> findManyByAttributes(Map<String, String> attributes) {
		return findByAttributesStream("attributes", attributes).spliterator();
	}

	@Override
	public T findByAttributes(Map<String, String> attributes, String attributesMapKey) {
		return findByKeyAttributes(attributesMapKey, attributes);
	}

	@Override
	public Spliterator<T> findManyByAttributes(Map<String, String> attributes, String attributesMapKey) {
		return findByAttributesStream(attributesMapKey, attributes).spliterator();
	}

	private T findByKeyAttributes(String fieldName, Map<String, String> attributes) {
		Stream<T> stream = findByAttributesStream(fieldName, attributes);
		return stream.findFirst().orElse(null);
	}

	private Stream<T> findByAttributesStream(String fieldName, Map<String, String> attributes) {
		Filter filter;
		if(attributes != null) {
			filter = Filters.and(attributes.entrySet().stream()
					.map(e -> Filters.equals(fieldName + "." + e.getKey(), e.getValue())).collect(Collectors.toList()));
		} else {
			filter = Filters.empty();
		}
		return collectionDriver.find(filter, null, null, null, 0);
	}

	@Override
	public Iterator<T> getAll() {
		return collectionDriver.find(Filters.empty(), null, null, null, 0).iterator();
	}
	
	public Stream<T> stream() {
		return collectionDriver.find(Filters.empty(), null, null, null, 0);
	}

	@Override
	public void remove(ObjectId id) {
		collectionDriver.remove(byId(id));
	}

	private Filters.Equals byId(ObjectId id) {
		return Filters.equals("id", id);
	}

	@Override
	public T save(T entity) {
		return collectionDriver.save(entity);
	}

	@Override
	public void save(Iterable<T> entities) {
		collectionDriver.save((Iterable<T>)entities);
	}

	@Override
	public List<T> getRange(int skip, int limit) {
		return collectionDriver.find(Filters.empty(), null, skip, limit, 0).collect(Collectors.toList());
	}
	
	protected void createOrUpdateIndex(String field) {
		collectionDriver.createOrUpdateIndex(field);
	}
	
	protected void createOrUpdateCompoundIndex(String... fields) {
		collectionDriver.createOrUpdateCompoundIndex(fields);
	}
}
