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
package ch.exense.commons.core.accessors.serialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

@SuppressWarnings("rawtypes")
public class DottedMapKeySerializer extends JsonSerializer<DottedKeyMap> {

	public DottedMapKeySerializer() {
		super();
	}

	@Override
	public void serialize(DottedKeyMap value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		Map<Object, Object> newMap = new HashMap<>();
		for(Object key:value.keySet()) {
			newMap.put(encodeKey(key), value.get(key));
		}
		jgen.writeObject(newMap);
	}
	
    // replacing "." and "$" by their unicodes as they are invalid keys in BSON
    private Object encodeKey(Object key) {
    	if(key instanceof String) {
    		return ((String) key).replace("\\\\", "\\\\\\\\").replace("\\$", "\\\\u0024").replace(".", "\\\\u002e");
    	} else {
    		return key;
    	}
    }

}
