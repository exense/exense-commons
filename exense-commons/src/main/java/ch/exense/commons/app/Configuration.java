/*******************************************************************************
 * (C) Copyright 2018 Jerome Comte and Dorian Cransac
 *
 * This file is part of exense Commons
 *
 * exense Commons is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * exense Commons is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with exense Commons.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ch.exense.commons.app;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
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
		this(propertyFile, null);
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
			if (placeholders != null) {
				String replacement = placeholders.get(key);
				m.appendReplacement(sb, replacement);
			} else {
				logger.warn("Not able to replace placeholder '" + key + "'.Placeholder map is null");
			}
		}
		m.appendTail(sb);
		return sb.toString();
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

	@Override
	public void close() throws IOException {
		if (fileWatchService != null) {
			fileWatchService.close();
		}
	}

}
