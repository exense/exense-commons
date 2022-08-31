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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import junit.framework.Assert;

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

	private void touchAndWait(File file, Object lock, final AtomicInteger updatedCount, int expected, long lastModified) throws InterruptedException {
		synchronized (lock) {
			file.setLastModified(lastModified);
			lock.wait(100);
		}
		Thread.sleep(1000);
		Assert.assertEquals(expected,updatedCount.get());
	}
}
