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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileHelper {

	private static final Logger logger = LoggerFactory.getLogger(FileHelper.class);

	public static final int DEFAULT_BUFFER_SIZE = 2048;
	
	/**
	 * Creates a temporary file that will be deleted on JVM exit
	 * @return the {@link File} of the temporary file
	 * @throws IOException if an error occurs during file creation
	 */
	public static File createTempFile() throws IOException {
		File file = Files.createTempFile(null, null).toFile();
		file.deleteOnExit();
		return file;
	}
	
	/**
	 * Creates a temporary folder
	 * @throws IOException if an error occurs during folder creation
	 */
	public static File createTempFolder() throws IOException {
		return createTempFolder(null);
	}
	
	/**
	 * Creates a temporary folder
	 * @param prefix the prefix string to be used in generating the folder's name
	 * @return the {@link File} of the temporary folder
	 * @throws IOException if an error occurs during folder creation
	 */
	public static File createTempFolder(String prefix) throws IOException {
		return Files.createTempDirectory(prefix).toFile();
	}

	/**
	 * Deletes a folder recursively
	 * @param folder the {@link File} to be deleted
	 */
	public static boolean deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					if (!f.delete()) {
						logger.warn("Could not delete file '"+f.getAbsolutePath()+"'");
					}
				}
			}
		}

		boolean deleted = folder.delete();
		if (!deleted) {
			logger.warn("Could not delete folder '"+folder.getAbsolutePath()+"'");
		}
		return deleted;
	}

	/**
	 * Deletes a folder recursively using Java 11 capabilities  which is at least twice faster
	 * @param folder the {@link File} to be deleted
	 */
	public static boolean deleteFolderWithWalkFileTree(File folder) {
		try {
			Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (exc != null) {
						logger.warn("Errors occurred while trying to delete the content of the the directory {}. We still try to delete the directory itself. ", dir.toAbsolutePath(), exc);
					};
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					logger.warn("Could not delete file '{}'. Reason {}", file.toAbsolutePath(), exc.getMessage());
					return FileVisitResult.CONTINUE;
				}
			});
			return true;
		} catch (IOException e) {
			logger.warn("Could not delete folder '{}'", folder.getAbsolutePath());
			return false;
		}
	}

	/**
	 * Deletes a folder recursively making sure the folder and his content can safely be deleted beforehand
	 * @param folder the {@link File} to be deleted
	 */
	public static boolean safeDeleteFolder(File folder) {
		return isFolderDeletable(folder) && deleteFolder(folder);
	}

	private static boolean isFolderDeletable(File folder) {
		return folder.renameTo(folder);
	}

	/**
	 * Deletes a folder recursively, retrying in case of error
	 * @param folder the {@link File} to be deleted
	 */
	public static void deleteFolderWithRetryOnError(File folder) {
		File[] files = folder.listFiles();
		// delete folder contents
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolderWithRetryOnError(f);
				} else {
					deleteFileWithRetryOnError(f);
				}
			}
		}
		// delete folder itself
		deleteFileWithRetryOnError(folder);
	}

	private static void deleteFileWithRetryOnError(File f) {
		try {
			Poller.waitFor(f::delete, 10_000);
		} catch (TimeoutException e) {
			//Final try, logging actual exception in case of error
			try {
				Files.delete(f.toPath());
			} catch (IOException ex) {
				logger.error("Unable to delete file " + f.getAbsolutePath(), ex);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
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
	 * @param file: the file
	 * @return the time that the file was last modified. 
	 */
	public static long getLastModificationDateRecursive(File file) {
		return computeLastModification(file);
	}

	protected static long computeLastModification(File file) {
		return computeLastModificationDateRecursive(file);
	}

	protected static long computeLastModificationDateRecursive(File file) {
		if (file.isDirectory()) {
			long lastModificationDate = file.lastModified();
			for (File f : Objects.requireNonNull(file.listFiles())) {
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
	 * @throws IOException if an error occurs during file unzip
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
	 * @throws IOException if an error occurs during file unzip
	 */
	public static void unzip(byte[] bytes, File target) throws IOException {
		unzip(new ByteArrayInputStream(bytes), target);
	}

	/**
	 * Extracts the zip provided as stream to the target folder provided as argument
	 * @param stream the {@link InputStream} of the zip to be extracted
	 * @param target the target folder to extract to
	 * @throws IOException if an error occurs during file unzip
	 */
	public static void unzip(InputStream stream, File target) throws IOException {
		try (ZipInputStream zip = new ZipInputStream(stream)){
			if (!target.exists()) {
				Files.createDirectory(target.toPath());
			} else if (!target.isDirectory()) {
				throw new IOException("The target should be a directory");
			}

			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				String currentEntry = entry.getName().replaceAll("\\\\","/");

				File destFile = new File(target.getAbsolutePath(), currentEntry);
				File destinationParent = destFile.getParentFile();

				Files.createDirectories(destinationParent.toPath());

				if (!entry.isDirectory()) {
					byte[] data = new byte[DEFAULT_BUFFER_SIZE];

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
	 * Extracts the zip file to the target folder provided as argument.
	 * When resourcesOnly is true it will skip all *.class files and META-INF except potentially useful runtime files like services or manifests.
	 * @param zipFile the zip file to be extracted
	 * @param target the target folder to extract to
	 * @param resourcesOnly whether to only include resources file for JAR archive
	 * @throws IOException if an error occurs during file unzip
	 */
	public static void unzip(File zipFile, File target, boolean resourcesOnly) throws IOException {
		// Create target directory if absent before canonicalizing,
		// since toRealPath() requires the path to already exist
		if (!target.exists()) {
			Files.createDirectories(target.toPath());
		} else if (!target.isDirectory()) {
			throw new IOException("The target should be a directory");
		}

		// Canonicalize target once outside the loop — resolves symlinks and relative segments
		final Path canonicalTarget = target.toPath().toRealPath();

		// Cache created directories to avoid redundant createDirectories calls
		Set<Path> createdDirs = new HashSet<>();
		createdDirs.add(canonicalTarget);

		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(zipFile), 64 * 1024);
			 ZipInputStream zip = new ZipInputStream(in)) {

			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				String name = entry.getName().replace("\\", "/");

				// Reject absolute paths in ZIP entries (e.g. /etc/passwd)
				if (Paths.get(name).isAbsolute()) {
					throw new IOException("ZIP entry with absolute path is not allowed: " + name);
				}

				// normalize() resolves syntactic ".." segments, combined with canonicalTarget
				// (which has symlinks resolved) this prevents all known path traversal variants
				Path destPath = canonicalTarget.resolve(name).normalize();
				if (!destPath.startsWith(canonicalTarget)) {
					throw new IOException("ZIP entry outside of target directory: " + name);
				}

				if (entry.isDirectory()) {
					if (createdDirs.add(destPath)) {
						Files.createDirectories(destPath);
					}
				} else if (!shouldSkip(name, resourcesOnly)) {
					// Ensure parent directory exists
					Path parent = destPath.getParent();
					if (parent != null && createdDirs.add(parent)) {
						Files.createDirectories(parent);
					}
					// Write directly to disk — no in-memory buffering of full entry
					Files.copy(zip, destPath, StandardCopyOption.REPLACE_EXISTING);
				}
				zip.closeEntry();
			}
		}
	}

	/**
	 * Extracts the zip file to the target folder provided as argument with a parallel implementation
	 * Compared to the unzip methods that can takes up to 30 seconds for a 200 MB archive with 200 files, this method is by a factor 3-4 faster
	 * However it needs to store the whole ZIP content in memory for parallel streaming
	 * @param zipFile the zip file to be extracted
	 * @param target the target folder to extract to
	 * @throws IOException if an error occurs during file unzip
	 */
	public static void unzipParallel(File zipFile, File target) throws IOException {
		unzipParallel(zipFile, target, false);
	}

	/**
	 * Extracts the zip file to the target folder provided as argument with a parallel implementation
	 * When resourcesOnly is true it will skip all *.class files and META-INF except potentially useful runtime files like services or manifests
	 * Compared to the unzip methods that can takes up to 30 seconds for a 200 MB archive with 200 files, this method completes in less than a second
	 * However it needs to store the whole ZIP content in memory for parallel streaming
	 * @param zipFile the zip file to be extracted
	 * @param target the target folder to extract to
	 * @param resourcesOnly whether to only include resources file for JAR archive
	 * @throws IOException if an error occurs during file unzip
	 */
	public static void unzipParallel(File zipFile, File target, boolean resourcesOnly) throws IOException {
		Map<String, byte[]> entries = new LinkedHashMap<>();

		// Create target directory if absent
		if (!target.exists()) {
			Files.createDirectories(target.toPath());
		} else if (!target.isDirectory()) {
			throw new IOException("The target should be a directory");
		}

		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(zipFile), 64 * 1024);
			 ZipInputStream zip = new ZipInputStream(in)) {

			ZipEntry entry;
			// Canonicalize target once outside the loop — resolves symlinks and relative segments
			final Path canonicalTarget;
			try {
				canonicalTarget = target.toPath().toRealPath();
			} catch (IOException e) {
				throw new IOException("Could not canonicalize target directory: " + target, e);
			}

			while ((entry = zip.getNextEntry()) != null) {
				String name = entry.getName().replace("\\", "/");

				// Reject absolute paths in ZIP entries (e.g. /etc/passwd)
				if (Paths.get(name).isAbsolute()) {
					throw new IOException("ZIP entry with absolute path is not allowed: " + name);
				}

				// normalize() resolves syntactic ".." segments, combined with canonicalTarget
				// (which has symlinks resolved) this prevents all known path traversal variants
				Path destPath = canonicalTarget.resolve(name).normalize();
				if (!destPath.startsWith(canonicalTarget)) {
					throw new IOException("ZIP entry outside of target directory: " + name);
				}
				if (!entry.isDirectory() && !shouldSkip(name, resourcesOnly)) {
					entries.put(name, zip.readAllBytes());
				}
				zip.closeEntry();
			}
		}

		// Pre-create all directories
		Set<Path> dirs = new HashSet<>();
		for (String name : entries.keySet()) {
			Path parent = target.toPath().resolve(name).getParent();
			if (parent != null) dirs.add(parent);
		}
		try {
			dirs.stream()
					.sorted(Comparator.comparingInt(Path::getNameCount))
					.forEach(d -> {
						try {
							Files.createDirectories(d);
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});

			// Write files in parallel
			entries.entrySet().parallelStream().forEach(e -> {
				try {
					Files.write(target.toPath().resolve(e.getKey()).normalize(), e.getValue());
				} catch (IOException ex) {
					throw new UncheckedIOException(ex);
				}
			});
		} catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

	private static boolean shouldSkip(String name, boolean resourcesOnly) {
		if (!resourcesOnly) {
			return false;
		}
		// Skip .class files
		if (name.endsWith(".class")) {
			return true;
		}
		// Skip META-INF except potentially useful runtime files like services or manifests
		if (name.startsWith("META-INF/")
				&& !name.startsWith("META-INF/services/")
				&& !name.equals("META-INF/MANIFEST.MF")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Extracts zip entry to file
	 * @param stream the {@link InputStream} of the zip to be extracted
	 * @return the extracted file
	 * @throws IOException if an error occurs during file unzip
	 */
	public static File unzip(InputStream stream, String entryName) throws IOException {
		File f = null;
		try (ZipInputStream zip = new ZipInputStream(stream)){
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().equals(entryName)) {
					f = FileHelper.createTempFile();				
					byte[] data = new byte[DEFAULT_BUFFER_SIZE];
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
	 * @throws IOException if an error occurs during file zip
	 */
	public static void zip(File directory, File target) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(target);
		zip(directory, fileOutputStream);
		fileOutputStream.close();
	}

	/**
	 * Create a zip file of the directory denoted by the {@link File} passed as argument
	 * @param directory the directory to be zipped
	 * @param target the path to the target zip file
	 * @param fileFilter filter for files or directories in directory
	 * @throws IOException if an error occurs during file zip
	 */
	public static void zip(File directory, File target, Function<File, Boolean> fileFilter) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(target);
		zip(directory, fileOutputStream, fileFilter);
		fileOutputStream.close();
	}
	
	/**
	 * Create a zip file of the directory denoted by the {@link File} passed as argument 
	 * @param directory the directory to be zipped
	 * @param out the output stream of the target zip file
	 * @throws IOException if an error occurs during file zip
	 */
	public static void zip(File directory, OutputStream out) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(out);
		zos.setLevel(ZipOutputStream.STORED);
		zip(directory, directory, zos, null);
		zos.close();
	}

	/**
	 * Create a zip file of the directory denoted by the {@link File} passed as argument
	 * @param directory the directory to be zipped
	 * @param out the output stream of the target zip file
	 * @param fileFilter filter for files or directories in directory
	 * @throws IOException if an error occurs during file zip
	 */
	public static void zip(File directory, OutputStream out, Function<File, Boolean> fileFilter) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(out);
		zos.setLevel(ZipOutputStream.STORED);
		zip(directory, directory, zos, fileFilter);
		zos.close();
	}

	/**
	 * Create a zip file of the directory denoted by the {@link File} passed as argument 
	 * @param directory the directory to be zipped
	 * @return the byte array of the target zip
	 * @throws IOException if an error occurs during file zip
	 */
	public static byte[] zip(File directory) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		zip(directory, out, null);
		return out.toByteArray();
	}

	private static void zip(File directory, File base, ZipOutputStream zos, Function<File, Boolean> fileFilter) throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[8192];
		int read = 0;
		for (File file : Objects.requireNonNull(files)) {
			if (fileFilter == null || Boolean.TRUE.equals(fileFilter.apply(file))) {
				if (file.isDirectory()) {
					// ZipOutputStream can handle directories by adding a forward-slash / after the folder name
					ZipEntry dirEntry = new ZipEntry(createZipEntryName(base, file) + "/");
					zos.putNextEntry(dirEntry);
					zip(file, base, zos, fileFilter);
				} else {
					FileInputStream in = new FileInputStream(file);
					ZipEntry entry = new ZipEntry(createZipEntryName(base, file));
					zos.putNextEntry(entry);
					while (-1 != (read = in.read(buffer))) {
						zos.write(buffer, 0, read);
					}
					in.close();
				}
			}
		}
	}

	private static String createZipEntryName(File base, File file) {
		return file.getPath().substring(base.getPath().length() + 1).replaceAll("\\\\", "/");
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
	public static void zipFile(ZipOutputStream zos, File file, String basePath) {
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
		try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
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
	 * @throws IOException if an error occurs when reading resource
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
	 * @throws IOException if an error occurs when reading resource
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
	 * @throws IOException if an error occurs during resource extraction
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
	 * @throws IOException if an error occurs during copy
	 */
	public static void copy(final InputStream input, final OutputStream output) throws IOException {
		copy(input, output, 2048);
	}

	/**
	 * Copy a {@link InputStream} to an {@link OutputStream}
	 * @param input the {@link InputStream} to be read
	 * @param output the target {@link OutputStream} 
	 * @param bufferSize the buffer size to be used
	 * @throws IOException if an error occurs during copy
	 */
	public static void copy(final InputStream input, final OutputStream output, int bufferSize) throws IOException {
		final byte[] buffer = new byte[bufferSize];
		int n = 0;
		while ((n = input.read(buffer)) > 0) {
			output.write(buffer, 0, n);
		}
	}

}
