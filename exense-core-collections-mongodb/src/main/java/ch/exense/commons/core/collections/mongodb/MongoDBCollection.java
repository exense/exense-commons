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
package ch.exense.commons.core.collections.mongodb;

import ch.exense.commons.core.model.accessors.AbstractIdentifiableObject;
import com.google.common.collect.Streams;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoNamespace;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;
import org.mongojack.ObjectMapperConfigurer;
import org.mongojack.SerializationOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.exense.commons.core.collections.Collection;
import ch.exense.commons.core.collections.Filter;
import ch.exense.commons.core.collections.SearchOrder;
import ch.exense.commons.core.collections.filesystem.AbstractCollection;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class MongoDBCollection<T> extends AbstractCollection<T> implements Collection<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(MongoDBCollection.class);

	protected static final String CSV_DELIMITER = ";";
	
	private final MongoClientSession mongoClientSession;

	private JacksonMongoCollection<T> collection;
	
	/**
	 * @param collectionName the name of the mongo collection
	 * @param entityClass the
	 */
	public MongoDBCollection(MongoClientSession mongoClientSession, String collectionName, Class<T> entityClass) {
		this.mongoClientSession = mongoClientSession;
		
		collection = JacksonMongoCollection.builder()
				.withObjectMapper(ObjectMapperConfigurer.configureObjectMapper(MongoDBCollectionJacksonMapperProvider.getObjectMapper()))
				.withSerializationOptions(SerializationOptions.builder().withSimpleFilterSerialization(true).build())
				.build(mongoClientSession.getMongoDatabase(), collectionName, entityClass, UuidRepresentation.JAVA_LEGACY);
	}

	/**
	 * @param columnName the name of the column (field)
	 * @return the distinct values of the column 
	 */
	@Override
	public List<String> distinct(String columnName, Filter filter) {
		Bson query = filterToQuery(filter);
		
		if (columnName.equals(AbstractIdentifiableObject.ID)) {
			return collection.distinct("_id", query, ObjectId.class).map(ObjectId::toString).into(new ArrayList<String>());
		} else {
			return collection.distinct(columnName, query, String.class).into(new ArrayList<String>());
		}
	}
	
	private Bson filterToQuery(Filter filter) {
		return new MongoDBFilterFactory().buildFilter(filter);
	}
	
	@Override
	public Stream<T> find(Filter filter, SearchOrder order, Integer skip, Integer limit, int maxTime) {
		Bson query = filterToQuery(filter);
		//long count = collection.estimatedDocumentCount();
		
		//CountOptions option = new CountOptions();
		//option.skip(0).limit(DEFAULT_LIMIT);
		//long countResults = collection.countDocuments(query, option);
		
		FindIterable<T> find = collection.find(query).maxTime(maxTime, TimeUnit.SECONDS);
		if(order!=null) {
			String attributeName = fixAttributeName(order.getAttributeName());
			Document sortDoc = new Document(attributeName, order.getOrder());
			find.sort(sortDoc);
		}
		if(skip!=null) {
			find.skip(skip);
		}
		if(limit!=null) {
			find.limit(limit);
		}
		MongoCursor<T> iterator;
		try {
			iterator = find.iterator();
		} catch (MongoExecutionTimeoutException e) {
			logger.error("Query execution exceeded timeout of " + maxTime + " " + TimeUnit.SECONDS);
			throw e;
		}
		
		Iterator<T> enrichedIterator = new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				T next = iterator.next();
				fixIdAfterRead(next);
				return next;
			}
		};
		
		return Streams.stream(enrichedIterator);
	}

	private String fixAttributeName(String attributeName) {
		if(attributeName.equals(AbstractIdentifiableObject.ID)) {
			attributeName = "_id";
		} else if (attributeName.contains("."+AbstractIdentifiableObject.ID)) {
			attributeName = attributeName.replace("."+AbstractIdentifiableObject.ID, "._id");
		} 
		return attributeName;
	}
	
	private void fixIdAfterRead(T next) {
		if(next instanceof ch.exense.commons.core.collections.Document) {
			ch.exense.commons.core.collections.Document document = (ch.exense.commons.core.collections.Document) next;
			Object id = document.get("_id");
			if(id instanceof ObjectId) {
				id = id.toString();
			}
			document.put(AbstractIdentifiableObject.ID, id);
			document.remove("_id");
		}
	}
	
	private void fixIdBeforeSave(T next) {
		if(next instanceof ch.exense.commons.core.collections.Document) {
			ch.exense.commons.core.collections.Document document = (ch.exense.commons.core.collections.Document) next;
			Object id = document.get(AbstractIdentifiableObject.ID);
			if(id instanceof String) {
				id = new ObjectId((String) id);
			}
			document.put("_id", id);
			document.remove(AbstractIdentifiableObject.ID);
		}
	}

	@Override
	public void remove(Filter filter) {
		collection.deleteMany(filterToQuery(filter));
	}

	@Override
	public T save(T entity) {
		if(getId(entity) == null) {
			setId(entity, new ObjectId());
		}
		fixIdBeforeSave(entity);
		collection.save(entity);
		fixIdAfterRead(entity);
		return entity;
	}

	@Override
	public void save(Iterable<T> entities) {
		entities.forEach(this::save);
	}

	@Override
	public void createOrUpdateIndex(String field) {
		createOrUpdateIndex(collection, field);
	}

	@Override
	public void createOrUpdateCompoundIndex(String... fields) {
		createOrUpdateCompoundIndex(collection, fields);
	}
	
	
	public static void createOrUpdateIndex(com.mongodb.client.MongoCollection<?> collection, String attribute) {
		Document index = getIndex(collection, attribute);
		if(index==null) {
			collection.createIndex(new Document(attribute,1));
		}
	}

	public static void createOrUpdateCompoundIndex(com.mongodb.client.MongoCollection<?> collection, String... attribute) {
		Document index = getIndex(collection, attribute);
		
		if(index==null) {
			Document newIndex = new Document();
			
			for(String s : attribute)
				newIndex.append(s, 1);

			collection.createIndex(newIndex);
		}
	}
	
	private static Document getIndex(com.mongodb.client.MongoCollection<?> collection, String... attribute) {
		HashSet<String> attributes = new HashSet<>(Arrays.asList(attribute));

		for(Document index:collection.listIndexes()) {  // inspect all indexes, looking for a match
			Object o = index.get("key");

			if(o instanceof Document) {
				Document d = ((Document)o);
				
				if(attributes.equals(d.keySet())) {
					return d;
				}
			}
		}
		return null;
	}

	@Override
	public void rename(String newName) {
		collection.renameCollection(new MongoNamespace(mongoClientSession.getMongoDatabase().getName(), newName));
	}

	@Override
	public void drop() {
		collection.drop();
	}
	
//	/**
//	 * Export data to CSV
//	 * @param query
//	 * @param columns
//	 * @param writer
//	 */
//	public void export(Filter filter, Map<String, CollectionField> columns, PrintWriter writer) {
//		Bson query = filterToQuery(filter);
//		FindIterable<BasicDBObject> find = collection.find(query);
//		MongoCursor<BasicDBObject> iterator;
//		iterator = find.iterator();
//		if (!iterator.hasNext()) {
//			return;
//		}
//		BasicDBObject basicDBObject = iterator.next();
//		//if column names not provided by the caller, get them from the collection
//		if (columns == null || columns.size() == 0) {
//			columns = getExportFields();
//		}
//		//if the collection has also no specification, dump all keys found in first object
//		if (columns == null || columns.size() == 0 && iterator.hasNext()) {
//			columns = getExportFields();		
//			for (String key : basicDBObject.keySet()) {
//				columns.put(key, new CollectionField(key,key));			
//			}
//		}
//		//write headers
//		columns.values().forEach((v)->{
//			String title = v.getTitle().replaceAll("^ID", "id");
//			writer.print(title);
//			writer.print(CSV_DELIMITER);
//		});
//		writer.println();
//		//Dump first row (required when writting all keys found in 1st object)
//		dumpRow(basicDBObject,columns,writer);
//
//		int count = 1;
//		while(iterator.hasNext()) {
//			count++;
//			basicDBObject = iterator.next();
//			dumpRow(basicDBObject,columns,writer);
//		}
//	}
//	
//	private void dumpRow(BasicDBObject basicDBObject, Map<String, CollectionField> fields, PrintWriter writer) {
//		fields.forEach((key,field)->{
//			Object value = basicDBObject.get(key);
//			if (value != null) {
//				String valueStr = field.getFormat().format(value);
//				if(valueStr.contains(CSV_DELIMITER)||valueStr.contains("\n")||valueStr.contains("\"")) {
//					valueStr = "\"" + valueStr.replaceAll("\"", "\"\"") + "\"";
//				}
//				writer.print(valueStr);
//			}
//			writer.print(CSV_DELIMITER);
//		});
//		writer.println();
//	}
//
//	protected Map<String, CollectionField> getExportFields() {
//		Map<String, CollectionField> result = new HashMap<String,CollectionField> ();
//		return result;
//	}
	
}
