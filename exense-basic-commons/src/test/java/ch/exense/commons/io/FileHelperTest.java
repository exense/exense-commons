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

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileHelperTest {

	@Test
	public void test() throws Exception {
		File tempFile = FileHelper.createTempFile();
		
		Path tempDirectory = Files.createTempDirectory(null);
		Path file1 = Paths.get(tempDirectory.toAbsolutePath()+"/file1");
		Files.write(file1, "TEST".getBytes());
		
		Path subfolder1 = Paths.get(tempDirectory.toAbsolutePath()+"/subfolder1");
		subfolder1.toFile().mkdirs();
		
		Path file2 = Paths.get(subfolder1.toAbsolutePath()+"/file2");
		Files.write(file2, "TEST".getBytes());
		
		FileHelper.zip(tempDirectory.toFile(), tempFile);

		// classloader should see all files and folders as zip entires
		try (URLClassLoader classLoader = new URLClassLoader(new URL[]{tempFile.toURI().toURL()}, null)) {
			URL url = classLoader.getResource("file1");
			assertNotNull(url);
			try (InputStream fileUrlStream = url.openStream()) {
				byte[] bytes = fileUrlStream.readAllBytes();
				assertTrue(bytes.length > 0);
			}

			url = classLoader.getResource("subfolder1/");
			assertNotNull(url);

			url = classLoader.getResource("subfolder1/file2");
			assertNotNull(url);
			try (InputStream fileUrlStream = url.openStream()) {
				byte[] bytes = fileUrlStream.readAllBytes();
				assertTrue(bytes.length > 0);
			}
		}

		byte[] bytes = FileHelper.zip(tempDirectory.toFile());
		
		Path targetDirectory = Files.createTempDirectory(null);
		long t1 = System.currentTimeMillis();
		Thread.sleep(10); // don't ask, otherwise the first Assert occasionally fails on some systems, probably because of FS caching/optimizations
		FileHelper.unzip(tempFile, targetDirectory.toFile());
		
		Path targetDirectory2 = Files.createTempDirectory(null);
		FileHelper.unzip(bytes, targetDirectory2.toFile());
		
		long lastModificationDate = FileHelper.getLastModificationDateRecursive(targetDirectory.toFile());
		
		assertTrue("lastModificationDate >= t1 failed, lastModificationDate="+lastModificationDate+"; t1="+t1, lastModificationDate>=t1);
		
		String contentFile1 = new String(Files.readAllBytes(Paths.get(targetDirectory.toAbsolutePath().toString(), "file1")));
		assertEquals("TEST", contentFile1);
		
		String contentFile2 = new String(Files.readAllBytes(Paths.get(targetDirectory.toAbsolutePath().toString(), "subfolder1", "file2")));
		assertEquals("TEST", contentFile2);
		
		
		FileHelper.deleteFolder(targetDirectory.toFile());
		assertFalse(targetDirectory.toFile().exists());
		
		FileHelper.deleteFolderOnExit(targetDirectory2.toFile());
		assertTrue(targetDirectory2.toFile().exists());
	}
	
	@Test
	public void testUnzipParallel() throws Exception {
		// Build a source directory with files at root level and in nested subdirectories
		Path sourceDir = Files.createTempDirectory(null);
		Path file1 = sourceDir.resolve("file1.txt");
		Files.write(file1, "CONTENT_FILE1".getBytes());

		Path file2 = sourceDir.resolve("file2.txt");
		Files.write(file2, "CONTENT_FILE2".getBytes());

		Path subDir = sourceDir.resolve("subdir");
		subDir.toFile().mkdirs();
		Path file3 = subDir.resolve("file3.txt");
		Files.write(file3, "CONTENT_FILE3".getBytes());

		Path deepDir = subDir.resolve("nested");
		deepDir.toFile().mkdirs();
		Path file4 = deepDir.resolve("file4.txt");
		Files.write(file4, "CONTENT_FILE4".getBytes());

		Path classFile1 = sourceDir.resolve("file1.class");
		Files.write(classFile1, "CLASS_CONTENT_FILE1".getBytes());

		Path classFile4 = deepDir.resolve("file4.class");
		Files.write(classFile4, "CLASS_CONTENT_FILE4".getBytes());

		Path metaInf = sourceDir.resolve("META-INF");
		metaInf.toFile().mkdirs();
		Path metaInfFile = metaInf.resolve("someMetaInfFile.txt");
		Files.write(metaInfFile, "META_INF_CONTENT_FILE".getBytes());

		// Zip the source directory
		File zipFile = FileHelper.createTempFile();
		FileHelper.zip(sourceDir.toFile(), zipFile);
		FileHelper.deleteFolder(sourceDir.toFile());
		assertFalse(sourceDir.toFile().exists());

		verifyNewUnzip(zipFile, true, false);

		verifyNewUnzip(zipFile, true, true);

		verifyNewUnzip(zipFile, false, false);

		verifyNewUnzip(zipFile, false, true);

		// Cleanup
		assertTrue(zipFile.delete());
		assertFalse(sourceDir.toFile().exists());
	}

	private static void verifyNewUnzip(File zipFile, boolean parallel, boolean resourcesOnly) throws IOException {
		// Unzip using unzipParallel
		Path targetDir = Files.createTempDirectory(null);
		if (parallel) {
			FileHelper.unzipParallel(zipFile, targetDir.toFile(), resourcesOnly);
		} else {
			FileHelper.unzip(zipFile, targetDir.toFile(), resourcesOnly);
		}

		// Verify all files exist with correct content
		String content1 = new String(Files.readAllBytes(targetDir.resolve("file1.txt")));
		assertEquals("CONTENT_FILE1", content1);

		String content2 = new String(Files.readAllBytes(targetDir.resolve("file2.txt")));
		assertEquals("CONTENT_FILE2", content2);

		String content3 = new String(Files.readAllBytes(targetDir.resolve("subdir/file3.txt")));
		assertEquals("CONTENT_FILE3", content3);

		String content4 = new String(Files.readAllBytes(targetDir.resolve("subdir/nested/file4.txt")));
		assertEquals("CONTENT_FILE4", content4);

		if (resourcesOnly) {
			assertThrows(NoSuchFileException.class, () -> Files.readAllBytes(targetDir.resolve("file1.class")));
			assertThrows(NoSuchFileException.class, () -> Files.readAllBytes(targetDir.resolve("subdir/nested/file4.class")));
			assertThrows(NoSuchFileException.class, () -> Files.readAllBytes(targetDir.resolve("META-INF/someMetaInfFile.txt")));
		} else {
			String classFileContent = new String(Files.readAllBytes(targetDir.resolve("file1.class")));
			assertEquals("CLASS_CONTENT_FILE1", classFileContent);

			String classFileContent4 = new String(Files.readAllBytes(targetDir.resolve("subdir/nested/file4.class")));
			assertEquals("CLASS_CONTENT_FILE4", classFileContent4);

			String someMetaInfFile = new String(Files.readAllBytes(targetDir.resolve("META-INF/someMetaInfFile.txt")));
			assertEquals("META_INF_CONTENT_FILE", someMetaInfFile);
		}

		// Cleanup
		FileHelper.deleteFolder(targetDir.toFile());
		assertFalse(targetDir.toFile().exists());
	}

	@Test
	public void testUnzipParallelTargetCreatedIfAbsent() throws Exception {
		// Source directory with a single file
		Path sourceDir = Files.createTempDirectory(null);
		Files.write(sourceDir.resolve("hello.txt"), "HELLO".getBytes());

		File zipFile = FileHelper.createTempFile();
		FileHelper.zip(sourceDir.toFile(), zipFile);

		// Target directory does NOT exist yet – unzipParallel must create it
		Path targetDir = Files.createTempDirectory(null);
		FileHelper.deleteFolder(targetDir.toFile());
		assertFalse("Pre-condition: target must not exist", targetDir.toFile().exists());

		FileHelper.unzipParallel(zipFile, targetDir.toFile());

		assertTrue("Target directory must have been created", targetDir.toFile().isDirectory());
		String content = new String(Files.readAllBytes(targetDir.resolve("hello.txt")));
		assertEquals("HELLO", content);

		FileHelper.deleteFolderWithWalkFileTree(sourceDir.toFile());
		FileHelper.deleteFolderWithWalkFileTree(targetDir.toFile());
		assertFalse(sourceDir.toFile().exists());
		assertFalse(targetDir.toFile().exists());
	}

	@Test
	public void test2() throws IOException {
		String readResource = FileHelper.readResource(getClass(), "testFile.txt");
		assertEquals("TEST FILE", readResource);
		
		byte[] readResourceAsByteArray = FileHelper.readResourceAsByteArray(getClass(), "testFile.txt");
		assertEquals("TEST FILE", new String(readResourceAsByteArray));
		
		String readClassLoaderResource = FileHelper.readClassLoaderResource(getClass().getClassLoader(), "testClassloaderResource.txt");
		assertEquals("TEST FILE", readClassLoaderResource);

		byte[] readClassLoaderResourceAsByteArray = FileHelper.readClassLoaderResourceAsByteArray(getClass().getClassLoader(), "testClassloaderResource.txt");
		assertEquals("TEST FILE", new String(readClassLoaderResourceAsByteArray));
		
		File classLoaderResourceAsFile = FileHelper.getClassLoaderResourceAsFile(getClass().getClassLoader(), "testClassloaderResource.txt");
		String content = new String(Files.readAllBytes(classLoaderResourceAsFile.toPath()));
		assertEquals("TEST FILE", content);
		
		File tempFile = FileHelper.createTempFile();
		FileHelper.copy(new FileInputStream(classLoaderResourceAsFile), new FileOutputStream(tempFile));
		String contentTempFile = new String(Files.readAllBytes(tempFile.toPath()));
		assertEquals("TEST FILE", contentTempFile);
		
		File tempFile2 = FileHelper.extractResourceToTempFile(getClass(), "testFile.txt");
		String contentTempFile2 = new String(Files.readAllBytes(tempFile2.toPath()));
		assertEquals("TEST FILE", contentTempFile2);
	}
}
