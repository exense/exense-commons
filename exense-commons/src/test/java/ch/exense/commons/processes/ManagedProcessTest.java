package ch.exense.commons.processes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import ch.exense.commons.io.FileHelper;
import ch.exense.commons.processes.ManagedProcess.ManagedProcessException;
import junit.framework.Assert;

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
			Assert.assertTrue(managedProcess.getId().startsWith("MyJavaProcess_"));
		}
	}
	
	@Test
	public void test3() throws IOException, ManagedProcessException, TimeoutException, InterruptedException {
		try(ManagedProcess managedProcess = new ManagedProcess(Arrays.asList(new String[] {"java", "-version"}))) {
			runAndTestProcess(managedProcess);
		}
	}
	
	@Test
	public void test4() throws IOException, ManagedProcessException, TimeoutException, InterruptedException {
		try(ManagedProcess managedProcess = new ManagedProcess("MyJavaProcess", Arrays.asList(new String[] {"java", "-version"}))) {
			runAndTestProcess(managedProcess);
			Assert.assertTrue(managedProcess.getId().startsWith("MyJavaProcess_"));
		}
	}
	
	@Test
	public void test5() throws IOException, ManagedProcessException, TimeoutException, InterruptedException {
		File tempFolder = FileHelper.createTempFolder();
		try(ManagedProcess managedProcess = new ManagedProcess("MyJavaProcess", Arrays.asList(new String[] {"java", "-version"}), tempFolder)) {
			runAndTestProcess(managedProcess);
			Assert.assertTrue(managedProcess.getId().startsWith("MyJavaProcess_"));
			Assert.assertEquals(managedProcess.getId(), tempFolder.listFiles()[0].getName());
		}
		// Assert that the process log have been deleted
		Assert.assertEquals(0, tempFolder.listFiles().length);
	}

	@Test
	public void testError() throws IOException, TimeoutException, InterruptedException {
		Exception actual = null;
		try(ManagedProcess managedProcess = new ManagedProcess("invalid cmd")) {
			runAndTestProcess(managedProcess);
		} catch (ManagedProcessException e) {
			actual = e;
		}
		Assert.assertNotNull(actual);
		Assert.assertTrue(actual.getMessage().startsWith("Unable to start the process "));
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
		Assert.assertNotNull(actual);
		Assert.assertEquals("Unable to start the process "+id+ " twice. The process has already been started.", actual.getMessage());
	}
	
	private void runAndTestProcess(ManagedProcess managedProcess)
			throws ManagedProcessException, TimeoutException, InterruptedException, IOException {
		managedProcess.start();
		int exitValue = managedProcess.waitFor(1000);
		String errOutput = new String(Files.readAllBytes(managedProcess.getProcessErrorLog().toPath()));
		String stdOutput = new String(Files.readAllBytes(managedProcess.getProcessOutputLog().toPath()));
		Assert.assertEquals(0, exitValue);
		Assert.assertTrue(errOutput.startsWith("java version"));
		Assert.assertTrue(stdOutput.startsWith(""));
	}

}
