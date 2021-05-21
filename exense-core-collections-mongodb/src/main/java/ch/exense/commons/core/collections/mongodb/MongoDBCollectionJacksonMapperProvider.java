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

import ch.exense.commons.core.accessors.serialization.DottedKeyMap;
import ch.exense.commons.core.accessors.serialization.DottedMapKeyDeserializer;
import ch.exense.commons.core.accessors.serialization.DottedMapKeySerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ch.exense.commons.core.accessors.DefaultJacksonMapperProvider;


import java.util.ArrayList;
import java.util.List;

class MongoDBCollectionJacksonMapperProvider {

	public static List<Module> modules = new ArrayList<>();

	static {
		modules.addAll(DefaultJacksonMapperProvider.getCustomModules());
		modules.add(new DefaultAccessorModule());
	}

	public static ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		modules.forEach(m -> objectMapper.registerModule(m));
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}

	private static class DefaultAccessorModule extends SimpleModule {

		private static final long serialVersionUID = 5544301456563146100L;

		public DefaultAccessorModule() {
			super();
			// MongoDB doesn't support keys with dots in documents. The following
			// serializers are responsible for escaping dots in keys to be able to store it
			// in MongoDB
			addSerializer(DottedKeyMap.class, new DottedMapKeySerializer());
			addDeserializer(DottedKeyMap.class, new DottedMapKeyDeserializer());
		}
	}

}
