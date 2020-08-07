/*******************************************************************************
 * (C) Copyright 2016 Jerome Comte and Dorian Cransac
 *  
 * This file is part of STEP
 *  
 * STEP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * STEP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with STEP.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ch.exense.commons.core.mongo.accessors.generic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

import javax.json.JsonObjectBuilder;
import javax.json.spi.JsonProvider;

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;

import ch.exense.commons.core.model.accessors.AbstractIdentifiableObject;
import ch.exense.commons.core.model.accessors.CRUDAccessor;
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
