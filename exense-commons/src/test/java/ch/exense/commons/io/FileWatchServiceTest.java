/*******************************************************************************
 * (C) Copyright 2016 Jerome Comte and Dorian Cransac
 *  
 * This file is part of STEP
 *  
 * STEP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * STEP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with STEP.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
