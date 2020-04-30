package ch.exense.commons.testing;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import ch.exense.commons.testing.Remote;
import step.handlers.javahandler.AbstractKeyword;

@RunWith(ExenseTestRunner.class)
@Category(Remote.class)
public abstract class AbstractRemoteTest extends AbstractKeyword {
	
	protected void assertNotNull(Object o, String error) {
		if (o==null) {
			output.setBusinessError(error);
		}
	}
	protected void assertTrue(boolean o, String error) {
		if (!o) {
			output.setBusinessError(error);
		}
	}
}
