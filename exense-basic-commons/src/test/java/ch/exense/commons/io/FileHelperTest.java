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
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

public class FileHelperTest {

    @Test
    public void test() throws Exception {
        File tempFile = FileHelper.createTempFile();

        Path tempDirectory = Files.createTempDirectory(null);
        Path file1 = Paths.get(tempDirectory.toAbsolutePath() + "/file1");
        Files.write(file1, "TEST".getBytes());

        Path subfolder1 = Paths.get(tempDirectory.toAbsolutePath() + "/subfolder1");
        subfolder1.toFile().mkdirs();

        Path file2 = Paths.get(subfolder1.toAbsolutePath() + "/file2");
        Files.write(file2, "TEST".getBytes());

        FileHelper.zip(tempDirectory.toFile(), tempFile);

        // classloader should see all files and folders as zip entires
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{tempFile.toURI().toURL()}, null)) {
            URL url = classLoader.getResource("file1");
            Assert.assertNotNull(url);
            try (InputStream fileUrlStream = url.openStream()) {
                byte[] bytes = fileUrlStream.readAllBytes();
                Assert.assertTrue(bytes.length > 0);
            }

            url = classLoader.getResource("subfolder1/");
            Assert.assertNotNull(url);

            url = classLoader.getResource("subfolder1/file2");
            Assert.assertNotNull(url);
            try (InputStream fileUrlStream = url.openStream()) {
                byte[] bytes = fileUrlStream.readAllBytes();
                Assert.assertTrue(bytes.length > 0);
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

        Assert.assertTrue("lastModificationDate >= t1 failed, lastModificationDate=" + lastModificationDate + "; t1=" + t1, lastModificationDate >= t1);

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
        String readResource = FileHelper.readResource(getClass(), "testFile.txt");
        Assert.assertEquals("TEST FILE", readResource);

        byte[] readResourceAsByteArray = FileHelper.readResourceAsByteArray(getClass(), "testFile.txt");
        Assert.assertEquals("TEST FILE", new String(readResourceAsByteArray));

        String readClassLoaderResource = FileHelper.readClassLoaderResource(getClass().getClassLoader(), "testClassloaderResource.txt");
        Assert.assertEquals("TEST FILE", readClassLoaderResource);

        byte[] readClassLoaderResourceAsByteArray = FileHelper.readClassLoaderResourceAsByteArray(getClass().getClassLoader(), "testClassloaderResource.txt");
        Assert.assertEquals("TEST FILE", new String(readClassLoaderResourceAsByteArray));

        File classLoaderResourceAsFile = FileHelper.getClassLoaderResourceAsFile(getClass().getClassLoader(), "testClassloaderResource.txt");
        String content = new String(Files.readAllBytes(classLoaderResourceAsFile.toPath()));
        Assert.assertEquals("TEST FILE", content);

        File tempFile = FileHelper.createTempFile();
        FileHelper.copy(new FileInputStream(classLoaderResourceAsFile), new FileOutputStream(tempFile));
        String contentTempFile = new String(Files.readAllBytes(tempFile.toPath()));
        Assert.assertEquals("TEST FILE", contentTempFile);

        File tempFile2 = FileHelper.extractResourceToTempFile(getClass(), "testFile.txt");
        String contentTempFile2 = new String(Files.readAllBytes(tempFile2.toPath()));
        Assert.assertEquals("TEST FILE", contentTempFile2);
    }
}
