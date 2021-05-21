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

import ch.exense.commons.app.Configuration;
import org.bson.types.ObjectId;
import ch.exense.commons.core.collections.Collection;
import ch.exense.commons.core.collections.CollectionFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCollectionFactory implements CollectionFactory {

	private final Map<String, Map<ObjectId, Object>> collections = new ConcurrentHashMap<>();
	
	public InMemoryCollectionFactory(Configuration configuration) {
		super();
	}

	@Override
	public void close() throws IOException {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Collection<T> getCollection(String name, Class<T> entityClass) {
		Map<ObjectId, Object> entities = collections.computeIfAbsent(name, k->new ConcurrentHashMap<ObjectId, Object>());
		return (Collection<T>) new InMemoryCollection<T>(entityClass, (Map<ObjectId, T>) entities);
	}

}
