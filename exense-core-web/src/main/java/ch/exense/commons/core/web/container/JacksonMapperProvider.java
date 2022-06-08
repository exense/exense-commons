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
package ch.exense.commons.core.web.container;

import java.io.IOException;

import jakarta.ws.rs.ext.ContextResolver;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;

public class JacksonMapperProvider implements ContextResolver<ObjectMapper> {
	 
    private final ObjectMapper mapper;
 
    public JacksonMapperProvider() {
        mapper = createMapper();
    }
 
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
 
    /**
     * @return an ObjectMapper for the UI or export layer
     */
    public static ObjectMapper createMapper() {
    	ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JSR353Module());
        mapper.registerModule(new JsonOrgModule());
        mapper.registerModule(new SimpleModule("jersey", new Version(1, 0, 0, null,null,null)) //
                        .addSerializer(_id, _idSerializer()) //
                        .addDeserializer(_id, _idDeserializer()));
        return mapper;
    }
 
    private static Class<ObjectId> _id = ObjectId.class;
 
    private static JsonDeserializer<ObjectId> _idDeserializer() {
        return new JsonDeserializer<ObjectId>() {
            public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return new ObjectId(jp.readValueAs(String.class));
            }
        };
    }
 
    private static JsonSerializer<Object> _idSerializer() {
        return new JsonSerializer<Object>() {
            public void serialize(Object obj, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonProcessingException {
                jsonGenerator.writeString(obj == null ? null : obj.toString());
            }
        };
    }
}
