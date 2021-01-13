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
package ch.exense.commons.core.serialization;

import com.fasterxml.jackson.databind.module.SimpleModule;

import ch.exense.commons.core.accessors.serialization.DottedKeyMap;
import ch.exense.commons.core.accessors.serialization.DottedMapKeyDeserializer;
import ch.exense.commons.core.accessors.serialization.DottedMapKeySerializer;

/**
 * Default Jackson module used for the serialization in the persistence layer (Jongo)
 * This module isn't used in the REST layer (Jersey) and can therefore be used to define serializers that only 
 * have to be used when persisting objects
 * 
 */
public class DefaultAccessorModule extends SimpleModule {

	private static final long serialVersionUID = 5544301456563146100L;

	public DefaultAccessorModule() {
		super();
		
		addSerializer(DottedKeyMap.class, new DottedMapKeySerializer());
		addDeserializer(DottedKeyMap.class, new DottedMapKeyDeserializer());
		
	}

}
