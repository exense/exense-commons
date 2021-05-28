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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

import org.bson.types.ObjectId;

public interface Accessor<T extends AbstractIdentifiableObject> {

	/**
	 * Get an object by id
	 * 
	 * @param id the UID of the object
	 * @return the object
	 */
	T get(ObjectId id);
	
	/**
	 * Get an object by id
	 * 
	 * @param id the UID of the object
	 * @return the object
	 */
	T get(String id);

	/**
	 * Find an object by default attributes. If multiple objects match these attributes, the first one will be returned
	 * 
	 * @param attributes the map of mandatory attributes of the object to be found
	 * @return the object
	 */
	T findByAttributes(Map<String, String> attributes);
	
	/**
	 * Find objects by attributes.
	 * 
	 * @param attributes the map of mandatory attributes of the object to be found
	 * @return an {@link Iterator} for the objects found
	 */
	Spliterator<T> findManyByAttributes(Map<String, String> attributes);

	Iterator<T> getAll();

	/**
	 * Find an object by attributes. If multiple objects match these attributes, the first one will be returned
	 * 
	 * @param attributes the map of mandatory attributes of the object to be found
	 * @param attributesMapKey the string representing the name (or "key") of the attribute map
	 * @return the object
	 */
	T findByAttributes(Map<String, String> attributes, String attributesMapKey);

	/**
	 * Find objects by attributes.
	 * 
	 * @param attributes the map of mandatory attributes of the object to be found
	 * @param attributesMapKey the string representing the name (or "key") of the attribute map
	 * @return an {@link Iterator} for the objects found
	 */
	Spliterator<T> findManyByAttributes(Map<String, String> attributes, String attributesMapKey);
	
	/**
	 * Get the range of objects specified by the skip/limit parameters browsing the collection 
	 * sorted by ID in the descending order  
	 * 
	 * @param skip the start index (inclusive) of the range
	 * @param limit the size of the range
	 * @return a {@link List} containing the objects of the specified range
	 */
	List<T> getRange(int skip, int limit);

	/**
	 * Load all object in memory and return them as a collection
	 * 
	 * @return a {@link List} containing the objects of the specified range
	 */
	Collection<T> getAllAsCollection();
}
