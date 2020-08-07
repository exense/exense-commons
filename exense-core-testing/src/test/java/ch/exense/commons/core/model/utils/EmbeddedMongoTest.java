package ch.exense.commons.core.model.utils;

import org.junit.Test;

import ch.exense.commons.core.model.utils.EmbeddedMongoTestbench;
import junit.framework.Assert;

public class EmbeddedMongoTest extends EmbeddedMongoTestbench{
	
	@Test
	public void testBenchItself() {
		Assert.assertEquals(true, true);
	}
}
