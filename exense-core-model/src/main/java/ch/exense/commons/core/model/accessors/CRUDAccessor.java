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

import org.bson.types.ObjectId;

public interface CRUDAccessor<T extends AbstractIdentifiableObject> extends Accessor<T> {

	void remove(ObjectId id);

	/**
	 * Save an entity. If an entity with the same id exists, it will be updated otherwise inserted. 
	 * 
	 * @param entity the entity instance to be saved
	 * @return the saved entity
	 */
	T save(T entity);

	/**
	 * Save a list of entities. 
	 * 
	 * @param entities the list of entities to be saved
	 */
	void save(Collection<? extends T> entities);
}
