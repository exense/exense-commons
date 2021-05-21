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

import java.util.HashMap;
import java.util.Map;

/**
 * This class extends {@link AbstractIdentifiableObject} and is used
 * as parent class for all the objects that should be organized or identified 
 * by custom attributes
 *
 */
public class AbstractOrganizableObject extends AbstractIdentifiableObject {
	
	protected Map<String, String> attributes;
	
	public static final String NAME = "name";
	public static final String VERSION = "version";
	
	public AbstractOrganizableObject() {
		super();
	}

	/**
	 * @return the attributes that identify this object's instance
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * Sets the attributes used to identify this object's instance
	 * 
	 * @param attributes the object's attributes as key-value pairs
	 */
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	/**
	 * Add an attribute to the attribute. This will initialize the map if it is null
	 * This method is not thread safe
	 * 
	 * @param key the name of the attribute
	 * @param value the value
	 */
	public void addAttribute(String key, String value) {
		if(attributes==null) {
			attributes = new HashMap<>();
		}
		attributes.put(key, value);
	}

	public boolean hasAttribute(String key) {
		if(attributes != null) {
			return attributes.containsKey(key);
		} else {
			return false;
		}
	}

	public String getAttribute(String key) {
		if(attributes != null) {
			return attributes.get(key);
		} else {
			return null;
		}
	}
}
