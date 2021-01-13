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

import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractContext {

	private ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();

	public Object get(Object key) {
		return attributes.get(key);
	}

	public Object put(String key, Object value) {
		return attributes.put(key, value);
	}
	
	public <T> Object put(Class<T> class_, T value) {
		return attributes.put(class_.getName(), value);
	}
	
	@SuppressWarnings("unchecked")
	public <T>T get(Class<T> class_) {
		return (T) attributes.get(class_.getName());
	}

	public ConcurrentHashMap<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(ConcurrentHashMap<String, Object> attributes) {
		this.attributes = attributes;
	}
	
}
