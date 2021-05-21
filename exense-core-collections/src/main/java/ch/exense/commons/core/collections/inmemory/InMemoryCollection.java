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
package ch.exense.commons.core.collections.inmemory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.PropertyUtils;
import org.bson.types.ObjectId;
import ch.exense.commons.core.accessors.DefaultJacksonMapperProvider;
import ch.exense.commons.core.collections.*;
import ch.exense.commons.core.collections.PojoFilters.PojoFilterFactory;
import ch.exense.commons.core.collections.filesystem.AbstractCollection;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class InMemoryCollection<T> extends AbstractCollection<T> implements Collection<T> {

	private final Class<T> entityClass;
	private final Map<ObjectId, T> entities;
	private final ObjectMapper mapper = DefaultJacksonMapperProvider.getObjectMapper();
	
	public InMemoryCollection() {
		super();
		entityClass = null;
		entities = new ConcurrentHashMap<>();
	}
	
	public InMemoryCollection(Class<T> entityClass, Map<ObjectId, T> entities) {
		super();
		this.entityClass = entityClass;
		this.entities = entities;
	}

	@Override
	public List<String> distinct(String columnName, Filter filter) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Stream<T> find(Filter filter, SearchOrder order, Integer skip, Integer limit, int maxTime) {
		Stream<T> stream = filteredStream(filter);
		if(order != null) {
			Comparator<T> comparing = Comparator.comparing(e->{
				try {
					return PropertyUtils.getProperty(e, order.getAttributeName()).toString();
				} catch (NoSuchMethodException e1) {
					return "";
				} catch (IllegalAccessException | InvocationTargetException e1) {
					throw new RuntimeException(e1);
				}
			});
			if(order.getOrder()<0) {
				comparing = comparing.reversed();
			}
			stream = stream.sorted(comparing);
		}
		if(skip != null) {
			stream = stream.skip(skip);
		}
		if(limit != null) {
			stream = stream.limit(limit);
		}
		return stream.map(e -> {
			if(entityClass == Document.class && !(e instanceof Document)) {
				return (T) mapper.convertValue(e, Document.class);
			} else if(e instanceof Document && entityClass != Document.class) {
				return mapper.convertValue(e, entityClass);
			} else {
				return e;
			}
		});
	}

	private Stream<T> filteredStream(Filter filter) {
		PojoFilter<T> pojoFilter = new PojoFilterFactory<T>().buildFilter(filter);
		return entityStream().filter(pojoFilter::test).sorted(new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					return getId(o1).compareTo(getId(o2));
				}
			});
	}

	private Stream<T> entityStream() {
		return entities.values().stream();
	}

	@Override
	public void remove(Filter filter) {
		filteredStream(filter).forEach(f->{
			entities.remove(getId(f));
		});
	}

	@Override
	public T save(T entity) {
		if (getId(entity) == null) {
			setId(entity, new ObjectId());
		}
		entities.put(getId(entity), entity);
		return entity;
	}

	@Override
	public void save(Iterable<T> entities) {
		if (entities != null) {
			entities.forEach(e -> save(e));
		}
	}

	@Override
	public void createOrUpdateIndex(String field) {
		
	}

	@Override
	public void createOrUpdateCompoundIndex(String... fields) {
		
	}

	@Override
	public void rename(String newName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void drop() {
		// TODO Auto-generated method stub
		
	}
}
