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

import java.util.Collection;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DocumentObject implements Map<String, Object> {

	private final Map<String, Object> map;

	public DocumentObject() {
		this(new HashMap<>());
	}

	public DocumentObject(Map<String, Object> map) {
		super();
		this.map = map;
	}

	@SuppressWarnings("unchecked")
	public DocumentObject getObject(String key) {
		Object object = get(key);
		if (object != null) {
			if (object instanceof Map) {
				return new DocumentObject((Map<String, Object>) object);
			} else {
				throw new RuntimeException("Value of " + key + " is not a map but " + object.getClass().getName());
			}
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<DocumentObject> getArray(String key) {
		Object object = get(key);
		if (object != null) {
			if (object instanceof List) {
				return (List<DocumentObject>) ((List<?>) object).stream()
						.map(e -> new DocumentObject((Map<String, Object>) e)).collect(Collectors.toList());
			} else {
				throw new RuntimeException("Value of " + key + " is not a map but " + object.getClass().getName());
			}
		} else {
			return null;
		}
	}

	public String getString(String key) {
		Object object = get(key);
		return object != null ? object.toString() : null;
	}

	public boolean getBoolean(String key) {
		Object object = get(key);
		if (object != null) {
			if (object instanceof Boolean) {
				return (Boolean) object;
			} else if (object instanceof String) {
				return Boolean.parseBoolean((String) object);
			} else {
				throw new RuntimeException("Value " + object + " is not a boolean");
			}
		} else {
			return false;
		}

	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public Object get(Object key) {
		return map.get(key);
	}

	public Object put(String key, Object value) {
		return map.put(key, value);
	}

	public Object remove(Object key) {
		return map.remove(key);
	}

	public void putAll(Map<? extends String, ? extends Object> m) {
		map.putAll(m);
	}

	public void clear() {
		map.clear();
	}

	public Set<String> keySet() {
		return map.keySet();
	}

	public Collection<Object> values() {
		return map.values();
	}

	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	public boolean equals(Object o) {
		return map.equals(o);
	}

	public int hashCode() {
		return map.hashCode();
	}

	public Object getOrDefault(Object key, Object defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}

	public void forEach(BiConsumer<? super String, ? super Object> action) {
		map.forEach(action);
	}

	public void replaceAll(BiFunction<? super String, ? super Object, ? extends Object> function) {
		map.replaceAll(function);
	}

	public Object putIfAbsent(String key, Object value) {
		return map.putIfAbsent(key, value);
	}

	public boolean remove(Object key, Object value) {
		return map.remove(key, value);
	}

	public boolean replace(String key, Object oldValue, Object newValue) {
		return map.replace(key, oldValue, newValue);
	}

	public Object replace(String key, Object value) {
		return map.replace(key, value);
	}

	public Object computeIfAbsent(String key, Function<? super String, ? extends Object> mappingFunction) {
		return map.computeIfAbsent(key, mappingFunction);
	}

	public Object computeIfPresent(String key,
			BiFunction<? super String, ? super Object, ? extends Object> remappingFunction) {
		return map.computeIfPresent(key, remappingFunction);
	}

	public Object compute(String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction) {
		return map.compute(key, remappingFunction);
	}

	public Object merge(String key, Object value,
			BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
		return map.merge(key, value, remappingFunction);
	}
}