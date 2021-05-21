package ch.exense.commons.core.collections.inmemory;

import ch.exense.commons.app.Configuration;
import ch.exense.commons.core.collections.AbstractCollectionTest;

public class InMemoryCollectionTest extends AbstractCollectionTest {

	public InMemoryCollectionTest() {
		super(new InMemoryCollectionFactory(new Configuration()));
	}

}
