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
package ch.exense.commons.app;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.exense.commons.io.FileWatchService;

public class Configuration implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

	private FileWatchService fileWatchService;

	private File propertyFile;

	private Properties properties;

	private Map<String, String> placeholders;

	public Configuration() {
		super();
		properties = new Properties();
	}

	public Configuration(File propertyFile) throws IOException {
		this(propertyFile, new HashMap<>());
	}

	public Configuration(File propertyFile, Map<String, String> placeholders) throws IOException {
		super();

		this.propertyFile = propertyFile;
		this.placeholders = placeholders;

		load();

		if (getPropertyAsBoolean("conf.scan", false)) {
			fileWatchService = new FileWatchService();
			fileWatchService.register(propertyFile, new Runnable() {
				@Override
				public void run() {
					try {
						load();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}

	public void load() throws FileNotFoundException, IOException {
		properties = new Properties();
		if (propertyFile != null) {
			String propertiesContent = new String(Files.readAllBytes(propertyFile.toPath()));
			String resolvedPropertiesContent = replacePlaceholders(propertiesContent);
			properties.load(new StringReader(resolvedPropertiesContent));
		}
	}

	private String replacePlaceholders(String configXml) {
		StringBuffer sb = new StringBuffer();
		Matcher m = Pattern.compile("\\$\\{(.+?)\\}").matcher(configXml);
		while (m.find()) {
            String key = m.group(1);
            if(placeholders!=null) {
            	String replacement = placeholders.get(key);
            	if(replacement == null) {
            		throw new RuntimeException("Missing placeholder '"+key+"'.");
            	} else {
            		m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            	}
            } else {
            	throw new RuntimeException("Unable to replace placeholders. Placeholder map is null. This should never occur.");
            }
        }
		m.appendTail(sb);
		return sb.toString();
	}
	
	public Properties getUnderlyingPropertyObject(){
		return this.properties;
	}
	
	public String getProperty(String name) {
		return properties.getProperty(name);
	}

	public String getProperty(String name, String defaultValue) {
		return properties.getProperty(name, defaultValue);
	}

	public void putProperty(String name, String value) {
		properties.put(name, value);
	}

	public Integer getPropertyAsInteger(String name) {
		return getPropertyAsInteger(name, null);
	}

	public Integer getPropertyAsInteger(String name, Integer defaultValue) {
		String prop = properties.getProperty(name);
		if (prop != null) {
			return Integer.parseInt(prop);
		} else {
			return defaultValue;
		}
	}
	
	public Long getPropertyAsLong(String name) {
		return getPropertyAsLong(name, null);
	}

	public Long getPropertyAsLong(String name, Long defaultValue) {
		String prop = properties.getProperty(name);
		if (prop != null) {
			return Long.parseLong(prop);
		} else {
			return defaultValue;
		}
	}

	public boolean getPropertyAsBoolean(String name) {
		return getPropertyAsBoolean(name, false);
	}

	public boolean hasProperty(String name) {
		return properties.containsKey(name);
	}

	public boolean getPropertyAsBoolean(String name, boolean defaultValue) {
		String prop = properties.getProperty(name);
		if (prop != null) {
			return Boolean.parseBoolean(prop);
		} else {
			return defaultValue;
		}
	}
	
	public File getPropertyAsFile(String name) {
		return getPropertyAsFile(name, null);
	}
	
	public File getPropertyAsFile(String name, File defaultValue) {
		String prop = properties.getProperty(name);
		if (prop != null) {
			return new File(prop);
		} else {
			return defaultValue;
		}
	}
	
	public File getPropertyAsDirectory(String name) {
		return getPropertyAsFile(name, null);
	}
	
	public File getPropertyAsDirectory(String name, File defaultValue) {
		String prop = properties.getProperty(name);
		File file;
		if (prop != null) {
			file = new File(prop);
		} else {
			file = defaultValue;
		}
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	public File getPropertyFile() {
		return propertyFile;
	}

	/**
	 * @return the {@link Set} of property names (keys) contained in this {@link Configuration}
	 */
	public Set<Object> getPropertyNames() {
		return properties.keySet();
	}

	public Map<String, String> getPlaceholders() {
		return placeholders;
	}

	@Override
	public void close() throws IOException {
		if (fileWatchService != null) {
			fileWatchService.close();
		}
	}

}
