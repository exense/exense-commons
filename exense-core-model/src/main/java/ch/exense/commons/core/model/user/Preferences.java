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
package ch.exense.commons.core.model.user;

import java.util.HashMap;
import java.util.Map;

public class Preferences {

	Map<String, Object> preferences = new HashMap<>();

	public Preferences() {
		super();
	}

	public Map<String, Object> getPreferences() {
		return preferences;
	}

	public void setPreferences(Map<String, Object> preferences) {
		this.preferences = preferences;
	}

	public String get(String key) {
		return (String) preferences.get(key);
	}

	public String getOrDefault(String key, String defaultValue) {
		return (String) preferences.getOrDefault(key, defaultValue);
	}
	
	public Object getAsBoolean(String key) {
		return (boolean) preferences.get(key);
	}

	public Object getOrDefaultAsBoolean(String key, boolean defaultValue) {
		return (boolean) preferences.getOrDefault(key, defaultValue);
	}

	public Object put(String key, Object value) {
		return preferences.put(key, value);
	}
	
	
}
