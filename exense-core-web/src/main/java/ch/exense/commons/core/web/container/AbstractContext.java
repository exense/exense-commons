/*******************************************************************************
 * (C) Copyright 2016 Jerome Comte and Dorian Cransac
 *  
 * This file is part of STEP
 *  
 * STEP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * STEP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with STEP.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
