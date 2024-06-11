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
package ch.exense.commons.io;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class FileWatchServiceTest {

	@Test
	public void testBasic() throws InterruptedException, IOException {
		FileWatchService fileWatchService = new FileWatchService();
		File file = FileHelper.getClassLoaderResourceAsFile(this.getClass().getClassLoader(),"FileWatchServiceTest.test");
		long lastModified = 0;
		file.setLastModified(lastModified);

		Object lock = new Object();

		final AtomicInteger updatedCount = new AtomicInteger(0);
		fileWatchService.setInterval(10);
		fileWatchService.register(file, new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					updatedCount.incrementAndGet();
					lock.notify();
				}
			}
		});


		touchAndWait(file, lock, updatedCount, 1, lastModified+10000);
		touchAndWait(file, lock, updatedCount, 2, lastModified+20000);

		fileWatchService.unregister(file);
		touchAndWait(file, lock, updatedCount, 2, lastModified+30000);

		fileWatchService.close();
	}

	@Test
	public void testConcurrentModification() throws Exception {

		// hook into logging so we can see if an exception was logged
		Logger logger = (Logger) LoggerFactory.getLogger(FileWatchService.class);

		AtomicInteger exceptions = new AtomicInteger();

		Appender<ILoggingEvent> appender = new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(ILoggingEvent event) {
				if (event.getLevel().equals(Level.ERROR)) {
					if (event.getThrowableProxy() != null && event.getThrowableProxy().getClassName().equals(ConcurrentModificationException.class.getName())) {
						exceptions.incrementAndGet();
					}
				}
			}
		};

		appender.start();
		logger.addAppender(appender);

		FileWatchService fileWatchService = new FileWatchService();
		File file = FileHelper.getClassLoaderResourceAsFile(this.getClass().getClassLoader(), "FileWatchServiceTest.test");
		File file2 = FileHelper.getClassLoaderResourceAsFile(this.getClass().getClassLoader(), "FileWatchServiceTest2.test");
		File file3 = FileHelper.getClassLoaderResourceAsFile(this.getClass().getClassLoader(), "FileWatchServiceTest3.test");

		long lastModified = 0;
		file.setLastModified(lastModified);

		fileWatchService.setInterval(500);

		Object lock = new Object();


		final AtomicInteger updatedCount = new AtomicInteger(0);

		AtomicReference<Runnable> runref = new AtomicReference<>();
		Runnable runnable = () -> {
			fileWatchService.unregister(file);
			fileWatchService.register(file, runref.get());
			synchronized (lock) {
				updatedCount.incrementAndGet();
				lock.notify();
			}
		};
		runref.set(runnable);

		fileWatchService.register(file2, () -> {});
		fileWatchService.register(file, runref.get());
		fileWatchService.register(file3, () -> {});

		for (int i=1; i <= 5; ++i) {
			touchAndWait(file, lock, updatedCount, i, i);
		}
		fileWatchService.close();

		Assert.assertEquals(0, exceptions.get());
	}

	private void touchAndWait(File file, Object lock, final AtomicInteger updatedCount, int expected, long lastModified) throws InterruptedException {
		synchronized (lock) {
			file.setLastModified(lastModified);
			lock.wait(100);
		}
		Thread.sleep(1000);
		Assert.assertEquals(expected, updatedCount.get());
	}
}
