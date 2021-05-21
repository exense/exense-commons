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
package ch.exense.commons.core.collections;

import ch.exense.commons.core.model.accessors.AbstractIdentifiableObject;
import org.bson.types.ObjectId;

import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;

public class Document extends DocumentObject {

	public Document() {
		super(new HashMap<>());
	}

	public Document(Map<String, Object> m) {
		super(m);
	}

	@Id
	public ObjectId getId() {
		return containsKey("_id") ? (ObjectId) get("_id") : new ObjectId((String) get(AbstractIdentifiableObject.ID));
	}
	
	public void setId(ObjectId id) {
		put(AbstractIdentifiableObject.ID, id.toString());
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
