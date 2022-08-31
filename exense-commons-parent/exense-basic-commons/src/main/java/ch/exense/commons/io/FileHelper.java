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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileHelper {

	public static final int DEFAULT_BUFFER_SIZE = 2048;
	
	/**
	 * Creates a temporary file that will be deleted on JVM exit
	 * @return the {@link File} of the temporary file
	 * @throws IOException
	 */
	public static File createTempFile() throws IOException {
		File file = Files.createTempFile(null, null).toFile();
		file.deleteOnExit();
		return file;
	}
	
	/**
	 * Creates a temporary folder
	 * @throws IOException
	 */
	public static File createTempFolder() throws IOException {
		File file = Files.createTempDirectory(null).toFile();
		return file;
	}
	
	/**
	 * Creates a temporary folder
	 * @param prefix the prefix string to be used in generating the folder's name
	 * @return the {@link File} of the temporary folder
	 * @throws IOException
	 */
	public static File createTempFolder(String prefix) throws IOException {
		File file = Files.createTempDirectory(prefix).toFile();
		return file;
	}

	/**
	 * Deletes a folder recursively
	 * @param folder the {@link File} to be deleted
	 */
	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	/**
	 * Requests that the directory denoted by this abstract pathname be deleted recursively when the virtual machine terminates
	 * @param folder the {@link File} to be deleted
	 */
	public static void deleteFolderOnExit(File folder) {
		folder.deleteOnExit();
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolderOnExit(f);
				} else {
					f.deleteOnExit();
				}
			}
		}
	}

	/**
	 * Computes the last modification date of a file or a folder recursively
	 * @param file 
	 * @return the time that the file was last modified. 
	 */
	public static final long getLastModificationDateRecursive(File file) {
		return computeLastModification(file);
	}

	protected static final long computeLastModification(File file) {
		return computeLastModificationDateRecursive(file);
	}

	protected static final long computeLastModificationDateRecursive(File file) {
		if (file.isDirectory()) {
			long lastModificationDate = file.lastModified();
			for (File f : file.listFiles()) {
				long lastChange = computeLastModificationDateRecursive(f);
				if (lastChange > lastModificationDate) {
					lastModificationDate = lastChange;
				}
			}
			return lastModificationDate;
		} else {
			return file.lastModified();
		}
	}

	/**
	 * Extracts the zip file to the target folder provided as argument
	 * @param zipFile the zip file to be extracted
	 * @param target the target folder to extract to
	 * @throws IOException
	 */
	public static void unzip(File zipFile, File target) throws IOException {
		try (FileInputStream in = new FileInputStream(zipFile)) {
			unzip(in, target);
		}
	}

	
	/**
	 * Extracts the zip provided as byte array to the target folder provided as argument
	 * @param bytes the byte array of the zip to be extracted
	 * @param target the target folder to extract to
	 * @throws IOException
	 */
	public static void unzip(byte[] bytes, File target) throws IOException {
		unzip(new ByteArrayInputStream(bytes), target);
	}

	/**
	 * Extracts the zip provided as stream to the target folder provided as argument
	 * @param stream the {@link InputStream} of the zip to be extracted
	 * @param target the target folder to extract to
	 * @throws IOException
	 */
	public static void unzip(InputStream stream, File target) throws IOException {
		try (ZipInputStream zip = new ZipInputStream(stream)){
			target.mkdir();

			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				String currentEntry = entry.getName().replaceAll("\\\\","/");

				File destFile = new File(target.getAbsolutePath(), currentEntry);
				File destinationParent = destFile.getParentFile();

				destinationParent.mkdirs();

				if (!entry.isDirectory()) {
					byte data[] = new byte[DEFAULT_BUFFER_SIZE];

					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, DEFAULT_BUFFER_SIZE);

					int currentByte;
					while ((currentByte = zip.read(data, 0, DEFAULT_BUFFER_SIZE)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
				}
			}
		}
	}
	
	/**
	 * Extracts zip entry to file
	 * @param stream the {@link InputStream} of the zip to be extracted
	 * @return the extracted file
	 * @throws IOException
	 */
	public static File unzip(InputStream stream, String entryName) throws IOException {
		File f = null;
		try (ZipInputStream zip = new ZipInputStream(stream)){
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().equals(entryName)) {
					f = FileHelper.createTempFile();				
					byte data[] = new byte[DEFAULT_BUFFER_SIZE];
					FileOutputStream fos = new FileOutputStream(f);
					BufferedOutputStream dest = new BufferedOutputStream(fos, DEFAULT_BUFFER_SIZE);
					int currentByte;
					while ((currentByte = zip.read(data, 0, DEFAULT_BUFFER_SIZE)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
				}
			}
		}
		return f;
	}
	/**
	 * Create a zip file of the directory denoted by the {@link File} passed as argument 
	 * @param directory the directory to be zipped
	 * @param target the path to the target zip file
	 * @throws IOException
	 */
	public static final void zip(File directory, File target) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(target);
		zip(directory, fileOutputStream);
		fileOutputStream.close();
	}
	
	/**
	 * Create a zip file of the directory denoted by the {@link File} passed as argument 
	 * @param directory the directory to be zipped
	 * @param out the output stream of the target zip file
	 * @throws IOException
	 */
	public static final void zip(File directory, OutputStream out) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(out);
		zos.setLevel(ZipOutputStream.STORED);
		zip(directory, directory, zos);
		zos.close();
	}

	/**
	 * Create a zip file of the directory denoted by the {@link File} passed as argument 
	 * @param directory the directory to be zipped
	 * @return the byte array of the target zip
	 * @throws IOException
	 */
	public static final byte[] zip(File directory) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		zip(directory, out);
		return out.toByteArray();
	}

	private static final void zip(File directory, File base, ZipOutputStream zos) throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[8192];
		int read = 0;
		for (int i = 0, n = files.length; i < n; i++) {
			if (files[i].isDirectory()) {
				zip(files[i], base, zos);
			} else {
				FileInputStream in = new FileInputStream(files[i]);
				ZipEntry entry = new ZipEntry(files[i].getPath().substring(base.getPath().length() + 1).replaceAll("\\\\","/"));
				zos.putNextEntry(entry);
				while (-1 != (read = in.read(buffer))) {
					zos.write(buffer, 0, read);
				}
				in.close();
			}
		}
	}
	
	/**
	 * Add provided file as byte array to the zip output stream
	 * @param zos zip output stream
	 * @param jsonStream bytearray output stream to be added to the zip
	 * @param name of the zip entry
	 */
	public static void zipFile(ZipOutputStream zos, ByteArrayOutputStream jsonStream, String name) throws IOException {
		ZipEntry entryJson = new ZipEntry(name.replaceAll("\\\\","/"));
		zos.putNextEntry(entryJson);
		zos.write(jsonStream.toByteArray());
	}
	
	/**
	 * Add provided file to the zip output stream removing the base path
	 * @param zos zip output stream
	 * @param file to be added to the zip
	 * @param basePath path of the file to be removed from the zip entry
	 */
	public static void zipFile(ZipOutputStream zos, File file, String basePath) throws IOException {
		try {
			byte[] buffer = new byte[8192];
			int read = 0;
			FileInputStream in = new FileInputStream(file);
			ZipEntry entry = new ZipEntry(file.getPath().substring(basePath.length() + 1).replaceAll("\\\\","/"));
			zos.putNextEntry(entry);
			while (-1 != (read = in.read(buffer))) {
				zos.write(buffer, 0, read);
			}
			in.close();
		} catch (IOException e) {
			throw new RuntimeException("Unable to create archive",e);
		}
	}
	
	/**
	 * Check whether the provided file is an archive
	 * @return the check result
	 */
	public static boolean isArchive(File f) {
	    int fileSignature = 0;
	    try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
	        fileSignature = raf.readInt();
	    } catch (IOException e) {
	        // handle if you like
	    }
	    return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
	}

	/**
	 * Reads the stream provided as argument using UTF8 charset
	 * @param is the stream to be read
	 * @return the content of the stream
	 */
	public static String readStream(InputStream is) {
		try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
			return scanner.useDelimiter("\\A").next().replaceAll("\r\n", "\n");
		}
	}

	/**
	 * Reads a resource using UTF8 charset
	 * @param clazz the class the resource is associated with
	 * @param resourceName the name of the resource
	 * @return the content of the resource
	 */
	public static String readResource(Class<?> clazz, String resourceName) {
		return readStream(clazz.getResourceAsStream(resourceName));
	}
	
	/**
	 * Reads a classloader resource using UTF8 charset
	 * @param classLoader the classloader to access the resource
	 * @param resourceName the name of the resource
	 * @return the content of the resource
	 */
	public static String readClassLoaderResource(ClassLoader classLoader, String resourceName) {
		return readStream(classLoader.getResourceAsStream(resourceName));
	}
	
	/**
	 * Reads a resource and returns its content as byte array
	 * @param clazz the class the resource is associated with
	 * @param resourceName the name of the resource
	 * @return the content of the resource as byte array
	 * @throws IOException
	 */
	public static byte[] readResourceAsByteArray(Class<?> clazz, String resourceName) throws IOException {
		try(InputStream resourceAsStream = clazz.getResourceAsStream(resourceName); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			copy(resourceAsStream, out);
			return out.toByteArray();
		}
	}
	
	/**
	 * Reads a classloader resource and returns its content as byte array
	 * @param classLoader the classloader to access the resource
	 * @param resourceName the name of the resource
	 * @return the content of the resource as byte array
	 * @throws IOException
	 */
	public static byte[] readClassLoaderResourceAsByteArray(ClassLoader classLoader, String resourceName) throws IOException {
		try(InputStream resourceAsStream = classLoader.getResourceAsStream(resourceName); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			copy(resourceAsStream, out);
			return out.toByteArray();
		}
	}
	
	/**
	 * @param classLoader the classloader to access the resource
	 * @param resourceName the name of the resource
	 * @return the resource as {@link File}
	 */
	public static File getClassLoaderResourceAsFile(ClassLoader classLoader, String resourceName) {
		try {
			URL url = classLoader.getResource(resourceName);
			// workaround: doing toURI().getPath() to decode %20 in case of spaces in path
			return url != null ? new File(url.toURI().getPath()) : null;
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while parsing URI of resource " + resourceName, e);
		}
	}
	
	/**
	 * Extract a resource and copy it to a a temporary file
	 * @param clazz the class the resource is associated with
	 * @param resourceName the name of the resource
	 * @return the temporary {@link File}
	 * @throws IOException
	 */
	public static File extractResourceToTempFile(Class<?> clazz, String resourceName) throws IOException {
		Path tempFile = Files.createTempFile("resourceName",".tmp");
		Files.write(tempFile, readResource(clazz, resourceName).getBytes(), StandardOpenOption.APPEND);
		tempFile.toFile().deleteOnExit();
		return tempFile.toFile();
	}
	
	/**
	 * Copy a {@link InputStream} to an {@link OutputStream} using a buffer size of 2048
	 * @param input the {@link InputStream} to be read
	 * @param output the target {@link OutputStream} 
	 * @throws IOException
	 */
	public static void copy(final InputStream input, final OutputStream output) throws IOException {
		copy(input, output, 2048);
	}

	/**
	 * Copy a {@link InputStream} to an {@link OutputStream}
	 * @param input the {@link InputStream} to be read
	 * @param output the target {@link OutputStream} 
	 * @param bufferSize the buffersize to be used 
	 * @throws IOException
	 */
	public static void copy(final InputStream input, final OutputStream output, int bufferSize) throws IOException {
		final byte[] buffer = new byte[bufferSize];
		int n = 0;
		while ((n = input.read(buffer)) > 0) {
			output.write(buffer, 0, n);
		}
	}

}
