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

import ch.exense.commons.io.FileHelper;
import ch.exense.commons.processes.ManagedProcess.ManagedProcessException;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class ManagedProcessTest  {

	@Test
	public void test() throws IOException, ManagedProcessException, TimeoutException, InterruptedException {
		try(ManagedProcess managedProcess = new ManagedProcess("java -version")) {
			runAndTestProcess(managedProcess);
		}
	}
	
	@Test
	public void test2() throws IOException, ManagedProcessException, TimeoutException, InterruptedException {
		try(ManagedProcess managedProcess = new ManagedProcess("MyJavaProcess", "java -version")) {
			runAndTestProcess(managedProcess);
			assertTrue(managedProcess.getId().startsWith("MyJavaProcess_"));
		}
	}
	
	@Test
	public void test3() throws IOException, ManagedProcessException, TimeoutException, InterruptedException {
		try(ManagedProcess managedProcess = new ManagedProcess(Arrays.asList("java", "-version"))) {
			runAndTestProcess(managedProcess);
		}
	}
	
	@Test
	public void test4() throws IOException, ManagedProcessException, TimeoutException, InterruptedException {
		try(ManagedProcess managedProcess = new ManagedProcess("MyJavaProcess", Arrays.asList("java", "-version"))) {
			runAndTestProcess(managedProcess);
			assertTrue(managedProcess.getId().startsWith("MyJavaProcess_"));
		}
	}
	
	@Test
	public void test5() throws IOException, ManagedProcessException, TimeoutException, InterruptedException {
		File tempFolder = FileHelper.createTempFolder();
		try(ManagedProcess managedProcess = new ManagedProcess("MyJavaProcess", Arrays.asList("java", "-version"), tempFolder, true)) {
			runAndTestProcess(managedProcess);
			assertTrue(managedProcess.getId().startsWith("MyJavaProcess_"));
			assertEquals(managedProcess.getId(), tempFolder.listFiles()[0].getName());
		}
		// Assert that the process log have been deleted
		assertEquals(0, tempFolder.listFiles().length);
	}

	@Test
	public void testClose() throws ManagedProcessException, InterruptedException, TimeoutException, IOException {
		File tempFolder = FileHelper.createTempFolder();
		ManagedProcess managedProcess = new ManagedProcess("MyJavaProcess", Arrays.asList("java", "-version"), tempFolder, true);
		runAndTestProcess(managedProcess);
		try {
			managedProcess.close();
		} catch (IOException e) {
			// Ensure that the method close throws an IOException to not break backward compatibility
			throw new RuntimeException(e);
		}

		// Assert that the process log have been deleted
		assertEquals(0, tempFolder.listFiles().length);
	}

	@Test
	public void testExecutionDirectory() throws IOException, ManagedProcessException, TimeoutException, InterruptedException {
		File tempExecutionFolder = FileHelper.createTempFolder();

		// Create a simple java program
		FileWriter fileWriter = new FileWriter(tempExecutionFolder.toPath().resolve("test.java").toFile());
		fileWriter.write("public class HelloWorld {\n" +
				"\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(\"Hello World!\");\n" +
				"        System.err.println(\"Error World\");\n" +
				"    }\n" +
				"\n" +
				"}");
		fileWriter.close();

		File tempLogFolder = FileHelper.createTempFolder();
		try(ManagedProcess managedProcess = new ManagedProcess("MyJavaProcess", Arrays.asList("java", "test.java"), tempExecutionFolder, tempLogFolder, true)) {
			managedProcess.startAndWaitFor(5000);
			assertTrue(managedProcess.getProcessOutputLogAsString().startsWith("Hello World!"));
			assertTrue(managedProcess.getProcessErrorLogAsString().startsWith("Error World"));
			String processLog = managedProcess.getProcessLog();
			assertTrue(processLog.contains("Hello World!"));
			assertTrue(processLog.contains("Error World"));

			// Assert that the process logs have been created within the log directory
			assertEquals(2, tempLogFolder.toPath().resolve(managedProcess.getId()).toFile().listFiles().length);
			// Assert that the process returns the correct execution directory
			assertEquals(tempExecutionFolder, managedProcess.getExecutionDirectory());
		}
		// Assert that the process log have been deleted
		assertEquals(0, tempLogFolder.listFiles().length);
		// Assert that the execution directory still exist
		assertEquals(1, tempExecutionFolder.listFiles().length);
	}

	@Test
	public void testExecutionDirectory2() throws IOException, ManagedProcessException, TimeoutException, InterruptedException {
		File tempExecutionFolder = FileHelper.createTempFolder();

		// Create a simple java program
		FileWriter fileWriter = new FileWriter(tempExecutionFolder.toPath().resolve("test.java").toFile());
		fileWriter.write("public class HelloWorld {\n" +
				"\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(\"Hello World!\");\n" +
				"        System.err.println(\"Error World\");\n" +
				"    }\n" +
				"\n" +
				"}");
		fileWriter.close();

		File logFolder = null;
		try(ManagedProcess managedProcess = new ManagedProcess("MyJavaProcess", Arrays.asList("java", "test.java"), tempExecutionFolder)) {
			managedProcess.startAndWaitFor(5000);
			assertTrue(managedProcess.getProcessOutputLogAsString().startsWith("Hello World!"));
			assertTrue(managedProcess.getProcessErrorLogAsString().startsWith("Error World"));
			String processLog = managedProcess.getProcessLog();
			assertTrue(processLog.contains("Hello World!"));
			assertTrue(processLog.contains("Error World"));

			logFolder = tempExecutionFolder.toPath().resolve(managedProcess.getId()).toFile();
			// Assert that the process logs have been created within the log directory
			assertEquals(2, logFolder.listFiles().length);
			// Assert that the process returns the correct execution directory
			assertEquals(tempExecutionFolder, managedProcess.getExecutionDirectory());
		}
		// Assert that the process log have been deleted
		assertFalse(logFolder.exists());
		// Assert that the execution directory still exist
		assertTrue(tempExecutionFolder.exists());
		assertEquals(1, tempExecutionFolder.listFiles().length);
	}

	@Test
	public void testInputOutputStream() throws Exception {
		File tempExecutionFolder = FileHelper.createTempFolder();

		// Create a simple java program that prints the content of the input stream
		FileWriter fileWriter = new FileWriter(tempExecutionFolder.toPath().resolve("test.java").toFile());
		fileWriter.write("public class HelloWorld {\n" +
				"    public static void main(String[] args) throws java.io.IOException {\n" +
				"        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));\n" +
				"        String name = reader.readLine();\n" +
				"        System.out.println(name);" +
				"    }\n" +
				"}");
		fileWriter.close();

		File tempLogFolder = FileHelper.createTempFolder();
		try(ManagedProcess managedProcess = new ManagedProcess("MyJavaProcess", Arrays.asList("java", "test.java"), tempExecutionFolder, tempLogFolder, false)) {
			managedProcess.start();
			OutputStreamWriter writer = new OutputStreamWriter(managedProcess.getProcessOutputStream());
			writer.write("hello\n");
			writer.close();
			managedProcess.waitFor(2000);

			InputStream processInputStream = managedProcess.getProcessInputStream();
			BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(processInputStream));
			String line = inputStreamReader.readLine();
			assertTrue(line.startsWith("hello"));
		}
	}

	@Test
	public void testError() throws IOException, TimeoutException, InterruptedException {
		Exception actual = null;
		try(ManagedProcess managedProcess = new ManagedProcess("invalid cmd")) {
			runAndTestProcess(managedProcess);
		} catch (ManagedProcessException e) {
			actual = e;
		}
		assertNotNull(actual);
		assertTrue(actual.getMessage().startsWith("Unable to start the process "));
	}
	
	@Test
	public void testMultipleStarts() throws IOException, TimeoutException, InterruptedException {
		Exception actual = null;
		String id = null;
		try(ManagedProcess managedProcess = new ManagedProcess("java -version")) {
			id = managedProcess.getId();
			runAndTestProcess(managedProcess);
			runAndTestProcess(managedProcess);
		} catch (ManagedProcessException e) {
			actual = e;
		}
		assertNotNull(actual);
		assertEquals("Unable to start the process "+id+ " twice. The process has already been started.", actual.getMessage());
	}
	
	private void runAndTestProcess(ManagedProcess managedProcess)
			throws ManagedProcessException, TimeoutException, InterruptedException {
		managedProcess.start();
		int exitValue = managedProcess.waitFor(1000);
		String errOutput = managedProcess.getProcessErrorLogAsString();
		String stdOutput = managedProcess.getProcessOutputLogAsString();
		assertEquals(0, exitValue);
		assertTrue(errOutput.contains("version"));
		assertTrue(stdOutput.isEmpty());
	}

}
