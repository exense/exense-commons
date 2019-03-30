package ch.exense.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import junit.framework.Assert;

public class FileHelperTest {

	@Test
	public void test() throws IOException {
		File tempFile = FileHelper.createTempFile();
		
		Path tempDirectory = Files.createTempDirectory(null);
		Path file1 = Paths.get(tempDirectory.toAbsolutePath()+"/file1");
		Files.write(file1, "TEST".getBytes());
		
		Path subfolder1 = Paths.get(tempDirectory.toAbsolutePath()+"/subfolder1");
		subfolder1.toFile().mkdirs();
		
		Path file2 = Paths.get(subfolder1.toAbsolutePath()+"/file2");
		Files.write(file2, "TEST".getBytes());
		
		FileHelper.zip(tempDirectory.toFile(), tempFile);
		
		byte[] bytes = FileHelper.zip(tempDirectory.toFile());
		
		Path targetDirectory = Files.createTempDirectory(null);
		long t1 = System.currentTimeMillis();
		FileHelper.unzip(tempFile, targetDirectory.toFile());
		
		Path targetDirectory2 = Files.createTempDirectory(null);
		FileHelper.unzip(bytes, targetDirectory2.toFile());
		
		long lastModificationDate = FileHelper.getLastModificationDateRecursive(targetDirectory.toFile());
		
		Assert.assertTrue(lastModificationDate>=t1);
		
		String contentFile1 = new String(Files.readAllBytes(Paths.get(targetDirectory.toAbsolutePath().toString(), "file1")));
		Assert.assertEquals("TEST", contentFile1);
		
		String contentFile2 = new String(Files.readAllBytes(Paths.get(targetDirectory.toAbsolutePath().toString(), "subfolder1", "file2")));
		Assert.assertEquals("TEST", contentFile2);
		
		
		FileHelper.deleteFolder(targetDirectory.toFile());
		Assert.assertFalse(targetDirectory.toFile().exists());
		
		FileHelper.deleteFolderOnExit(targetDirectory2.toFile());
		Assert.assertTrue(targetDirectory2.toFile().exists());
	}
	
	@Test
	public void test2() throws IOException {
		String resourceContent = FileHelper.readResource(getClass(), "testFile.txt");
		Assert.assertEquals("TEST FILE", resourceContent);
		
		File classLoaderResourceAsFile = FileHelper.getClassLoaderResourceAsFile(getClass(), "testClassloaderResource.txt");
		String content = new String(Files.readAllBytes(classLoaderResourceAsFile.toPath()));
		Assert.assertEquals("TEST FILE", content);
		
		File tempFile = FileHelper.createTempFile();
		FileHelper.copy(new FileInputStream(classLoaderResourceAsFile), new FileOutputStream(tempFile));
		String contentTempFile = new String(Files.readAllBytes(tempFile.toPath()));
		Assert.assertEquals("TEST FILE", contentTempFile);
	}
}
