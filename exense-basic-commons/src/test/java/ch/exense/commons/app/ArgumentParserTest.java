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

import org.junit.Test;

import junit.framework.Assert;

public class ArgumentParserTest {

	@Test
	public void test() {
		ArgumentParser argumentParser = new ArgumentParser(new String[] {"-param1=value1","-param2=value2"});
		Assert.assertTrue(argumentParser.hasOption("param1"));
		Assert.assertTrue(argumentParser.hasOption("param2"));
		String param1 = argumentParser.getOption("param1");
		Assert.assertEquals("value1", param1);
	}

}
