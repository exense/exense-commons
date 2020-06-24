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
