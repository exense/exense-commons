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

import org.junit.Test;

import ch.exense.commons.io.FileHelper;
import junit.framework.Assert;

public class ConfigurationTest {

	@Test
	public void test() throws IOException {
		try(Configuration configuration = new Configuration(FileHelper.getClassLoaderResourceAsFile(this.getClass().getClassLoader(), "test.properties"))) {
			Assert.assertEquals("myProp1", configuration.getProperty("my.prop1"));
			Assert.assertEquals((int)1000, (int)configuration.getPropertyAsInteger("my.prop2"));
			Assert.assertEquals(false, configuration.getPropertyAsBoolean("my.prop3"));
			Assert.assertEquals(100000000000000l, (long)configuration.getPropertyAsLong("my.prop5"));
			Assert.assertEquals(new File("."), configuration.getPropertyAsFile("my.prop6"));
			Assert.assertEquals(new File("."), configuration.getPropertyAsDirectory("my.prop6"));
			
			Assert.assertEquals(true, configuration.hasProperty("my.prop5"));
			
			configuration.putProperty("my.prop1", "changedProp");
			Assert.assertEquals("changedProp", configuration.getProperty("my.prop1"));
			
			configuration.putProperty("my.prop2", "1001");
			Assert.assertEquals((int)1001, (int)configuration.getPropertyAsInteger("my.prop2"));
			
			Assert.assertEquals("default", configuration.getProperty("notExistingProp","default"));
			Assert.assertEquals((int)1005, (int)configuration.getPropertyAsInteger("notExistingProp",1005));
			Assert.assertEquals((long)1000000l, (long)configuration.getPropertyAsLong("notExistingProp",1000000l));
			Assert.assertEquals(false, configuration.getPropertyAsBoolean("notExistingProp",false));
			File defaultValue = new File("notExisting");
			Assert.assertEquals(defaultValue, configuration.getPropertyAsFile("notExistingProp",defaultValue));
			File tempFolder = FileHelper.createTempFolder();
			File myFolder = new File(tempFolder+"/myFolder");
			Assert.assertEquals(myFolder, configuration.getPropertyAsDirectory("notExistingProp",myFolder));
			Assert.assertTrue(myFolder.exists());
			FileHelper.deleteFolder(tempFolder);
		}
	}
	
	@Test
	public void testScan() throws IOException, InterruptedException {
		File propertyFile = FileHelper.createTempFile();
		FileHelper.copy(this.getClass().getClassLoader().getResourceAsStream("testScan.properties"), new FileOutputStream(propertyFile));
		try(Configuration configuration = new Configuration(propertyFile)) {
			Assert.assertEquals("myProp1", configuration.getProperty("my.prop1"));
			Assert.assertEquals((int)1000, (int)configuration.getPropertyAsInteger("my.prop2"));
			Assert.assertEquals(false, configuration.getPropertyAsBoolean("my.prop3"));
			Assert.assertEquals(100000000000000l, (long)configuration.getPropertyAsLong("my.prop5"));

			Thread.sleep(2000);
			
			Files.write(propertyFile.toPath(), new String("\nmyNewProp=myNewPropsValue").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.SYNC);
			
			Thread.sleep(2000);
			
			Assert.assertEquals("myNewPropsValue", configuration.getProperty("myNewProp"));
			
			Assert.assertEquals(propertyFile, configuration.getPropertyFile());
		}
	}
	
	@Test
	public void testPlaceholder() throws IOException {
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("myPlaceholder", "myPlaceholdersValue");
		try(Configuration configuration = new Configuration(FileHelper.getClassLoaderResourceAsFile(this.getClass().getClassLoader(), "testPlaceholders.properties"), placeholders)) {
			Assert.assertEquals("myPlaceholdersValue", configuration.getProperty("my.prop4"));
		}
	}

}
