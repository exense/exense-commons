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

import java.util.HashMap;

/**
 * 
 * A special Map that is serialized by {@link DottedMapKeySerializer}
 * when persisted in the DB. This serializer supports the persistence of keys
 * that contain "." and "$" which are normally not allowed as key by Mongo.
 * 
 * 
 */
public class DottedKeyMap<K, V>  extends HashMap<K, V> {

	private static final long serialVersionUID = 8922169005470741941L;

}
