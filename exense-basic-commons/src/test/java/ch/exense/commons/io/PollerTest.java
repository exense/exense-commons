package ch.exense.commons.io;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class PollerTest {

	@Test
	public void test() throws TimeoutException, InterruptedException {
		AtomicInteger count = new AtomicInteger();
		Poller.waitFor(()->count.incrementAndGet()==5, 1000);
	}

}
