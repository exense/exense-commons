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

import ch.exense.commons.core.model.accessors.AbstractIdentifiableObject;
import org.bson.types.ObjectId;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

/**
 * This {@link Accessor} loads all the entities of the provided underlying
 * {@link Accessor} at initialization and keeps them in memory for further
 * accesses. Write operations like remove and save are persisted in the
 * underlying {@link Accessor}
 *
 * @param <T> the type of the entity
 */
public class CachedAccessor<T extends AbstractIdentifiableObject> implements Accessor<T> {

	private final Accessor<T> cache = new InMemoryAccessor<T>();

	private final Accessor<T> underlyingAccessor;

	/**
	 * @param underlyingAccessor the {@link Accessor} from which the entities should
	 *                           be loaded
	 */
	public CachedAccessor(Accessor<T> underlyingAccessor) {
		super();
		this.underlyingAccessor = underlyingAccessor;
		reloadCache();
	}

	/**
	 * Reloads all the entities from the underlying {@link Accessor}
	 */
	public void reloadCache() {
		// Load cache
		underlyingAccessor.getAll().forEachRemaining(e -> cache.save(e));
	}

	@Override
	public T get(ObjectId id) {
		return cache.get(id);
	}

	@Override
	public T get(String id) {
		return cache.get(id);
	}

	@Override
	public T findByAttributes(Map<String, String> attributes) {
		return cache.findByAttributes(attributes);
	}

	@Override
	public Spliterator<T> findManyByAttributes(Map<String, String> attributes) {
		return cache.findManyByAttributes(attributes);
	}

	@Override
	public T findByAttributes(Map<String, String> attributes, String attributesMapKey) {
		return cache.findByAttributes(attributes, attributesMapKey);
	}

	@Override
	public Spliterator<T> findManyByAttributes(Map<String, String> attributes, String attributesMapKey) {
		return cache.findManyByAttributes(attributes, attributesMapKey);
	}

	@Override
	public Iterator<T> getAll() {
		return cache.getAll();
	}

	@Override
	public void remove(ObjectId id) {
		cache.remove(id);
		underlyingAccessor.remove(id);
	}

	@Override
	public T save(T entity) {
		T result = underlyingAccessor.save(entity);
		cache.save(result);
		return result;
	}

	@Override
	public void save(Iterable<T> entities) {
		entities.forEach(e -> save(e));
	}

	@Override
	public List<T> getRange(int skip, int limit) {
		return cache.getRange(skip, limit);
	}
}
