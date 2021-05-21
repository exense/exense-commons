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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

// Used to deserialize Map<String, Object>. Per default jackson deserialize the map values as Map
public class MapSerializer extends JsonSerializer<Map<String, Object>> {

	private ObjectMapper mapper;

	public MapSerializer() {
		super();
		mapper = MapDeserializer.getMapper();
	}

	@Override
	public void serialize(Map<String, Object> value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		mapper.writeValue(gen, value);
	}
}
