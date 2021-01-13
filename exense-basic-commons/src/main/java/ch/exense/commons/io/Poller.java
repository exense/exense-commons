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

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class Poller {

	/**
	 * Waits for the predicate to return true using a default polling rate of 100ms
	 * @param predicate the predicate to be tested
	 * @param timeout the timeout in ms
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public static void waitFor(Supplier<Boolean> predicate, long timeout) throws TimeoutException, InterruptedException {
		waitFor(predicate, timeout, 100);
	}
	
	/**
	 * Waits for the predicate to return true
	 * @param predicate the predicate to be tested
	 * @param timeout the timeout in ms
	 * @param pollingIntervalMs the polling interval in ms
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public static void waitFor(Supplier<Boolean> predicate, long timeout, long pollingIntervalMs) throws TimeoutException, InterruptedException {
		long t1 = System.currentTimeMillis();
		while(timeout == 0 || System.currentTimeMillis()<t1+timeout) {
			boolean result = predicate.get();
			if(result) {
				return;
			}
			Thread.sleep(pollingIntervalMs);
		}
		throw new TimeoutException();
	}
}
