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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import ch.exense.commons.io.FileHelper;

public class ConfigurationTest {

    @Test
    public void test() throws IOException {
        try (Configuration configuration = new Configuration(FileHelper.getClassLoaderResourceAsFile(this.getClass().getClassLoader(), "test.properties"))) {
            Assert.assertEquals("myProp1", configuration.getProperty("my.prop1"));
            Assert.assertEquals((int) 1000, (int) configuration.getPropertyAsInteger("my.prop2"));
            Assert.assertEquals(false, configuration.getPropertyAsBoolean("my.prop3"));
            Assert.assertEquals(100000000000000L, (long) configuration.getPropertyAsLong("my.prop5"));
            Assert.assertEquals(new File("."), configuration.getPropertyAsFile("my.prop6"));
            Assert.assertEquals(new File("."), configuration.getPropertyAsDirectory("my.prop6"));

            Assert.assertEquals(true, configuration.hasProperty("my.prop5"));

            configuration.putProperty("my.prop1", "changedProp");
            Assert.assertEquals("changedProp", configuration.getProperty("my.prop1"));

            configuration.putProperty("my.prop2", "1001");
            Assert.assertEquals((int) 1001, (int) configuration.getPropertyAsInteger("my.prop2"));

            Assert.assertEquals("default", configuration.getProperty("notExistingProp", "default"));
            Assert.assertEquals((int) 1005, (int) configuration.getPropertyAsInteger("notExistingProp", 1005));
            Assert.assertEquals((long) 1000000l, (long) configuration.getPropertyAsLong("notExistingProp", 1000000l));
            Assert.assertEquals(false, configuration.getPropertyAsBoolean("notExistingProp", false));
            File defaultValue = new File("notExisting");
            Assert.assertEquals(defaultValue, configuration.getPropertyAsFile("notExistingProp", defaultValue));
            File tempFolder = FileHelper.createTempFolder();
            File myFolder = new File(tempFolder + "/myFolder");
            Assert.assertEquals(myFolder, configuration.getPropertyAsDirectory("notExistingProp", myFolder));
            Assert.assertTrue(myFolder.exists());
            FileHelper.deleteFolder(tempFolder);
        }
    }

    @Test
    public void testScan() throws IOException, InterruptedException {
        File propertyFile = FileHelper.createTempFile();
        FileHelper.copy(this.getClass().getClassLoader().getResourceAsStream("testScan.properties"), new FileOutputStream(propertyFile));
        try (Configuration configuration = new Configuration(propertyFile)) {
            Assert.assertEquals("myProp1", configuration.getProperty("my.prop1"));
            Assert.assertEquals((int) 1000, (int) configuration.getPropertyAsInteger("my.prop2"));
            Assert.assertEquals(false, configuration.getPropertyAsBoolean("my.prop3"));
            Assert.assertEquals(100000000000000l, (long) configuration.getPropertyAsLong("my.prop5"));

            Thread.sleep(2000);

            Files.write(propertyFile.toPath(), new String("\nmyNewProp=myNewPropsValue").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.SYNC);

            Thread.sleep(2000);

            Assert.assertEquals("myNewPropsValue", configuration.getProperty("myNewProp"));

            Assert.assertEquals(propertyFile, configuration.getPropertyFile());
        }
    }

    @Test
    public void testPlaceholder() throws IOException {
        ArgumentParser argumentParser = new ArgumentParser(new String[]{"-myPlaceholder=myPlaceholdersValue"});
        try (Configuration configuration = new Configuration(FileHelper.getClassLoaderResourceAsFile(this.getClass().getClassLoader(), "testPlaceholders.properties"), argumentParser.getOptions())) {
            Assert.assertEquals("myPlaceholdersValue", configuration.getProperty("my.prop4"));
        }
    }

    @Test
    public void testPlaceholderInComments() throws IOException {
        File propertyFile = FileHelper.createTempFile();
        String content = "# commented.prop=${missing1}\n" +
            "   ! commented.prop2=${missing2}\r\n" +
            "my.prop1=a # ${myPlaceholder}\n" +
            "my.prop2=first \\\n" +
            "    #${myPlaceholder}\r" +
            "# comment ending with a backslash \\\n" +
            "my.prop3=${myPlaceholder}\n" +
            "my.prop4=escaped\\\\\n" +
            "#${missing3}";
        Files.write(propertyFile.toPath(), content.getBytes());
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("myplaceholder", "value");

        // The placeholders of the comments aren't resolved, the ones of the properties are
        try (Configuration configuration = new Configuration(propertyFile, placeholders)) {
            Assert.assertEquals("a # value", configuration.getProperty("my.prop1"));
            Assert.assertEquals("first #value", configuration.getProperty("my.prop2"));
            Assert.assertEquals("value", configuration.getProperty("my.prop3"));
            Assert.assertEquals("escaped\\", configuration.getProperty("my.prop4"));
            Assert.assertNull(configuration.getProperty("commented.prop"));
            Assert.assertEquals(4, configuration.getUnderlyingPropertyObject().size());
        }

        // A missing placeholder of a property is still reported
        Files.write(propertyFile.toPath(), "my.prop=${missing}".getBytes());
        RuntimeException e = Assert.assertThrows(RuntimeException.class, () -> new Configuration(propertyFile, placeholders, key -> null));
        Assert.assertEquals("Missing placeholder 'missing': not defined as argument nor as environment variable.", e.getMessage());
    }

    @Test
    public void testPlaceholderFromEnvironmentVariable() throws IOException {
        File propertyFile = FileHelper.createTempFile();
        Files.write(propertyFile.toPath(), ("my.prop1=${myPlaceholder}\n" +
            "my.prop2=${MY_ENV_VAR}\n" +
            "my.prop3=${BOTH}").getBytes());
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("myplaceholder", "fromArgument");
        placeholders.put("both", "fromArgument");
        Map<String, String> environmentVariables = Map.of("MY_ENV_VAR", "fromEnvironment", "BOTH", "fromEnvironment",
            "myPlaceholder", "fromEnvironment");

        // The placeholder map takes precedence, the environment variables are used for the placeholders missing from it
        try (Configuration configuration = new Configuration(propertyFile, placeholders, environmentVariables::get)) {
            Assert.assertEquals("fromArgument", configuration.getProperty("my.prop1"));
            Assert.assertEquals("fromEnvironment", configuration.getProperty("my.prop2"));
            Assert.assertEquals("fromArgument", configuration.getProperty("my.prop3"));
        }

        // The environment variables are case-sensitive
        Files.write(propertyFile.toPath(), "my.prop=${my_env_var}".getBytes());
        RuntimeException e = Assert.assertThrows(RuntimeException.class,
            () -> new Configuration(propertyFile, placeholders, environmentVariables::get));
        Assert.assertEquals("Missing placeholder 'my_env_var': not defined as argument nor as environment variable.", e.getMessage());

        // Without placeholder map, only the environment variables are used
        Files.write(propertyFile.toPath(), "my.prop=${MY_ENV_VAR}".getBytes());
        try (Configuration configuration = new Configuration(propertyFile, null, environmentVariables::get)) {
            Assert.assertEquals("fromEnvironment", configuration.getProperty("my.prop"));
        }
    }
}
