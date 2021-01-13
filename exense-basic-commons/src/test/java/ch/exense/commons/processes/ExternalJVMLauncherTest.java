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
package ch.exense.commons.processes;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import ch.exense.commons.processes.ManagedProcess.ManagedProcessException;

public class ExternalJVMLauncherTest {

	private static final String OUTPUT = "OK";

	public static void main(String[] args) {
		System.out.print(OUTPUT);
	}

	@Test
	public void test() throws ManagedProcessException, IOException, TimeoutException, InterruptedException {
		ExternalJVMLauncher externalJVMLauncher = new ExternalJVMLauncher(javaExe(), new File("log"));
		try (ManagedProcess externalVM = externalJVMLauncher.launchExternalJVM("MyExternalProcess", ExternalJVMLauncherTest.class,
				new ArrayList<>(), new ArrayList<>())) {
			externalVM.waitFor(1000);
			String output = new String(Files.readAllBytes(externalVM.getProcessOutputLog().toPath()));
			assertEquals(OUTPUT, output);
		}
	}

	private static String javaExe() {
		final String JAVA_HOME = System.getProperty("java.home");
		final File BIN = new File(JAVA_HOME, "bin");
		File exe = new File(BIN, "java");

		if (!exe.exists()) {
			// We might be on Windows, which needs an exe extension
			exe = new File(BIN, "java.exe");
		}

		if (exe.exists()) {
			return exe.getAbsolutePath();
		}

		try {
			// Just try invoking java from the system path; this of course
			// assumes "java[.exe]" is /actually/ Java
			final String NAKED_JAVA = "java";
			new ProcessBuilder(NAKED_JAVA).start();

			return NAKED_JAVA;
		} catch (IOException e) {
			return null;
		}
	}

}
