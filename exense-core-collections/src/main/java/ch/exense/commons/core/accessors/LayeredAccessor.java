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
import com.google.common.collect.Streams;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LayeredAccessor<T extends AbstractIdentifiableObject> implements Accessor<T> {

	private final List<Accessor<T>> accessors = new ArrayList<>();
	
	public LayeredAccessor() {
		super();
	}
	
	public LayeredAccessor(List<? extends Accessor<T>> accessors) {
		super();
		this.accessors.addAll(accessors);
	}

	public void addAccessor(Accessor<T> accessor) {
		accessors.add(accessor);
	}

	public void pushAccessor(Accessor<T> accessor) {
		accessors.add(0, accessor);
	}

	@Override
	public T get(ObjectId id) {
		return layeredLookup(a->a.get(id));
	}

	protected <V> V layeredLookup(Function<Accessor<T>, V> f) {
		for (Accessor<T> Accessor : accessors) {
			V result = f.apply(Accessor);
			if(result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	public T get(String id) {
		return get(new ObjectId(id));
	}

	@Override
	public T findByAttributes(Map<String, String> attributes) {
		return layeredLookup(a->a.findByAttributes(attributes));
	}

	@Override
	public Spliterator<T> findManyByAttributes(Map<String, String> attributes) {
		return layeredMerge(a->a.findManyByAttributes(attributes));
	}

	protected <V> Spliterator<V> layeredMerge(Function<Accessor<T>, Spliterator<V>> f) {
		List<V> result = new ArrayList<>();
		accessors.forEach(a->{
			f.apply(a).forEachRemaining(result::add);	
		});
		return result.spliterator();
	}
	
	@Override
	public Iterator<T> getAll() {
		Iterator<Accessor<T>> accessorIterator = accessors.iterator();
		return new MergeIterator<T>(accessorIterator);
	}

	private static final class MergeIterator<T extends AbstractIdentifiableObject> implements Iterator<T> {
		
		private final Iterator<Accessor<T>> accessorIterator;
		private Iterator<T> currentIterator;

		private MergeIterator(Iterator<Accessor<T>> accessorIterator) {
			this.accessorIterator = accessorIterator;
			nextAccessor();
		}

		@Override
		public boolean hasNext() {
			if(currentIterator != null) {
				boolean hasNext = currentIterator.hasNext();
				if(hasNext) {
					return true;
				} else {
					nextAccessor();
					return hasNext();
				}
			} else {
				return false;
			}
		}

		protected void nextAccessor() {
			if(accessorIterator.hasNext()) {
				Accessor<T> currentAccessor = accessorIterator.next();
				currentIterator = currentAccessor.getAll();
			} else {
				currentIterator = null;
			}
		}

		@Override
		public T next() {
			if(hasNext()) {
				return currentIterator.next();
			} else {
				throw new NoSuchElementException();
			}
		}
	}
	
	@Override
	public T findByAttributes(Map<String, String> attributes, String attributesMapKey) {
		return layeredLookup(a->a.findByAttributes(attributes, attributesMapKey));
	}

	@Override
	public Spliterator<T> findManyByAttributes(Map<String, String> attributes, String attributesMapKey) {
		return layeredMerge(a->a.findManyByAttributes(attributes, attributesMapKey));
	}

	@Override
	public List<T> getRange(int skip, int limit) {
		List<T> all = Streams.stream(getAll()).collect(Collectors.toList());
		return all.subList(skip, Math.min(all.size(), skip+limit));
	}

	@Override
	public void remove(ObjectId id) {
		for (Accessor<T> Accessor : accessors) {
			T e = Accessor.get(id);
			if(e!= null) {
				Accessor.remove(id);
			}
		}
	}

	@Override
	public T save(T entity) {
		return getAccessorForPersistence().save(entity);
	}

	@Override
	public void save(Iterable<T> entities) {
		getAccessorForPersistence().save(entities);
	}

	protected Accessor<T> getAccessorForPersistence() {
		return accessors.get(0);
	}
}
