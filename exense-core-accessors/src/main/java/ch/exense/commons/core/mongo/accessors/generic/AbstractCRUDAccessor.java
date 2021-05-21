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
package ch.exense.commons.core.mongo.accessors.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;

import javax.json.JsonObjectBuilder;
import javax.json.spi.JsonProvider;

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;

import ch.exense.commons.core.model.accessors.AbstractIdentifiableObject;
import ch.exense.commons.core.mongo.MongoClientSession;

public class AbstractCRUDAccessor<T extends AbstractIdentifiableObject> extends AbstractAccessor implements CRUDAccessor<T> {
			
	protected MongoCollection collection;
	
	private Class<T> entityClass;

	protected static JsonProvider jsonProvider = jsonProvider = JsonProvider.provider();
		
	public AbstractCRUDAccessor(MongoClientSession clientSession, String collectionName, Class<T> entityClass) {
		super(clientSession);
		this.entityClass = entityClass;
		collection = getJongoCollection(collectionName);
	}
	
	@Override
	public T get(ObjectId id) {
		T entity = collection.findOne(id).as(entityClass);
		return entity;
	}

	@Override
	public T get(String id) {
		return get(new ObjectId(id));
	}
	
	@Override
	public T findByAttributes(Map<String, String> attributes) {
		String query = queryByAttributes(attributes);
		return collection.findOne(query).as(entityClass);
	}
	
	@Override
	public Spliterator<T> findManyByAttributes(Map<String, String> attributes) {
		String query = queryByAttributes(attributes);
		return collection.find(query).as(entityClass).spliterator();
	}
	
	@Override
	public T findByAttributes(Map<String, String> attributes, String attributesMapKey) {
		String query = queryByAttributes(attributes, attributesMapKey);
		return collection.findOne(query).as(entityClass);
	}
	
	@Override
	public Spliterator<T> findManyByAttributes(Map<String, String> attributes, String attributesMapKey) {
		String query = queryByAttributes(attributes, attributesMapKey);
		return collection.find(query).as(entityClass).spliterator();
	}
	
	protected String queryByAttributes(Map<String, String> attributes) {
		return queryByAttributes(attributes, "attributes");
	}

	protected String queryByAttributes(Map<String, String> attributes, String attributesMapKey) {
		JsonObjectBuilder builder = jsonProvider.createObjectBuilder();
		String prefix = "";
		if(attributesMapKey != null && !attributesMapKey.isEmpty()) {
			prefix = attributesMapKey + ".";
		}
		for(String key:attributes.keySet()) {
			builder.add(prefix+key, attributes.get(key));
		}

		String query = builder.build().toString();
		return query;
	}
	
	@Override
	public Iterator<T> getAll() {
		return collection.find().as(entityClass);
	}
	
	@Override
	public Collection<T> getAllAsCollection() {
		Set<T> all = new HashSet<>();
		getAll().forEachRemaining(t -> all.add(t));
		return all;
	}
	
	@Override
	public void remove(ObjectId id) {
		collection.remove(id);
	}
	
	@Override
	public T save(T entity) {
		collection.save(entity);
		return entity;
	}
	
	@Override
	public void save(java.util.Collection<? extends T> entities) {
		this.collection.insert(entities.toArray());
	}

	@Override
	public List<T> getRange(int skip, int limit) {
		ArrayList<T> result = new ArrayList<T>();
		collection.find().sort("{_id:-1}").skip(skip).limit(limit).as(entityClass).forEachRemaining(e->result.add(e));
		return result;
	}
	
	public Class<T> getEntityClass() {
		return entityClass;
	}
}
