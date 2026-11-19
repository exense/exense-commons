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

import ch.exense.commons.io.FileWatchService;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Configuration implements Closeable {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    private FileWatchService fileWatchService;

    private File propertyFile;

    private Properties properties;

    private Map<String, String> placeholders;

    private Function<String, String> environmentVariables;

    public Configuration() {
        super();
        properties = new Properties();
    }

    public Configuration(File propertyFile) throws IOException {
        this(propertyFile, new HashMap<>());
    }

    /**
     * @param placeholders the values of the placeholders, by lower case name. The placeholders missing from this map are
     *                     read from the environment variables of the same name (case-sensitive).
     */
    public Configuration(File propertyFile, Map<String, String> placeholders) throws IOException {
        this(propertyFile, placeholders, System::getenv);
    }

    Configuration(File propertyFile, Map<String, String> placeholders, Function<String, String> environmentVariables) throws IOException {
        super();

        this.propertyFile = propertyFile;
        this.placeholders = placeholders;
        this.environmentVariables = environmentVariables;

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

    public void load() throws IOException {
        properties = new Properties();
        if (propertyFile != null) {
            String propertiesContent = new String(Files.readAllBytes(propertyFile.toPath()));
            // The placeholders are replaced after parsing: the comments are thereby ignored, and the placeholder values
            // are taken as-is instead of being interpreted by the properties format (e.g. '\' as escape character)
            Properties rawProperties = new Properties();
            rawProperties.load(new StringReader(propertiesContent));
            for (String name : rawProperties.stringPropertyNames()) {
                // The placeholders also being replaced in keys mimics the previous full-file replacement behavior and
                // is therefore bug-compatible with old versions. Arguably, we should only evaluate placeholders in values?
                properties.setProperty(replacePlaceholders(name), replacePlaceholders(rawProperties.getProperty(name)));
            }
        }
    }

    /**
     * @return the given property name or value, with its placeholders replaced by their values
     */
    private String replacePlaceholders(String text) {
        StringBuilder sb = new StringBuilder();
        Matcher m = PLACEHOLDER_PATTERN.matcher(text);
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(getPlaceholderValue(m.group(1))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * @return the value of the placeholder from the placeholder map, otherwise from the environment variable of the
     * same name
     */
    private String getPlaceholderValue(String key) {
        String value = placeholders != null ? placeholders.get(key.toLowerCase()) : null;
        if (value == null) {
            value = environmentVariables.apply(key);
        }
        if (value == null) {
            throw new RuntimeException("Missing placeholder '" + key + "': not defined as argument nor as environment variable.");
        }
        return value;
    }

    public Properties getUnderlyingPropertyObject() {
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
        if (!file.exists()) {
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
