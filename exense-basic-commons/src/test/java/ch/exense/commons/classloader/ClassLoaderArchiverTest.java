package ch.exense.commons.classloader;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.*;

import static org.junit.Assert.*;

public class ClassLoaderArchiverTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    // ─── helpers ──────────────────────────────────────────────────────────────

    private static Set<String> entryNames(File archive) throws IOException {
        Set<String> names = new HashSet<>();
        try (JarFile jar = new JarFile(archive)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                names.add(entries.nextElement().getName());
            }
        }
        return names;
    }

    private static byte[] readEntry(File archive, String entryName) throws IOException {
        try (JarFile jar = new JarFile(archive)) {
            JarEntry entry = jar.getJarEntry(entryName);
            if (entry == null) return null;
            try (InputStream is = jar.getInputStream(entry)) {
                return is.readAllBytes();
            }
        }
    }

    /**
     * Creates a minimal JAR containing one regular resource and several
     * signature files (.SF, .RSA, .DSA, .EC) to verify that they are stripped
     * when the JAR is merged into a fat archive.
     */
    private File createJarWithSignatureFiles() throws IOException {
        File jar = tmp.newFile("signed.jar");
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar))) {
            for (String sig : new String[]{"META-INF/TEST.SF", "META-INF/TEST.RSA", "META-INF/TEST.DSA", "META-INF/TEST.EC"}) {
                jos.putNextEntry(new JarEntry(sig));
                jos.write("dummy-signature".getBytes());
                jos.closeEntry();
            }
            jos.putNextEntry(new JarEntry("signed-resource.txt"));
            jos.write("content".getBytes());
            jos.closeEntry();
        }
        return jar;
    }

    // ─── directory structure preserved (bug fix) ──────────────────────────────

    @Test
    public void testDirectoryStructurePreservedInFullArchive() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive);

        Set<String> entries = entryNames(archive);

        // Resources under src/test/resources/folder/ must keep their sub-path
        assertTrue("folder/File.txt must be stored under its subdirectory",
                entries.contains("folder/File.txt"));
        assertTrue("folder/File2.txt must be stored under its subdirectory",
                entries.contains("folder/File2.txt"));
        // Deeper nesting (step/reporting/...) must also be preserved
        assertTrue("Deeply nested resources must keep their full path",
                entries.stream().anyMatch(e -> e.startsWith("step/reporting/")));
    }

    @Test
    public void testDirectoryStructurePreservedInResourcesOnlyArchive() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive, ClassLoaderArchiver.getResourceFilter());

        Set<String> entries = entryNames(archive);

        assertTrue("folder/File.txt must be stored under its subdirectory",
                entries.contains("folder/File.txt"));
        assertTrue("folder/File2.txt must be stored under its subdirectory",
                entries.contains("folder/File2.txt"));
    }

    @Test
    public void testFileContentIsPreservedVerbatim() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive);

        byte[] expected;
        try (InputStream is = ClassLoaderArchiverTest.class.getResourceAsStream("/folder/File.txt")) {
            assertNotNull("/folder/File.txt must be on the test classpath", is);
            expected = is.readAllBytes();
        }

        byte[] actual = readEntry(archive, "folder/File.txt");
        assertNotNull("folder/File.txt must be present in the archive", actual);
        assertArrayEquals("Content of folder/File.txt must be preserved verbatim", expected, actual);
    }

    // ─── URLClassLoader resource access (including directories) ──────────────

    /**
     * Verifies that a {@link URLClassLoader} created from the archive can locate
     * both file resources and directory resources.  Directory lookup requires
     * explicit directory entries (names ending with {@code /}) to be present in
     * the JAR — this test reproduces the failure that occurs when they are absent.
     */
    @Test
    public void testURLClassLoaderCanAccessFileAndDirectoryResources() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive);

        // Use null parent so lookups go only to our archive, not the test classpath
        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{archive.toURI().toURL()}, null)) {

            // A plain file resource must always be reachable
            URL fileResource = classLoader.getResource("folder/File.txt");
            assertNotNull("URLClassLoader must find file resources", fileResource);

            // A directory resource requires a directory entry (name ending with '/')
            // to be present in the JAR; without it getResource returns null.
            URL dirResourceWithSlash = classLoader.getResource("folder/");
            assertNotNull(
                    "URLClassLoader must find directory resources with trailing slash — JAR must contain directory entries",
                    dirResourceWithSlash);

            // ZipFile.getEntry() retries with name+"/" when the bare name is not found,
            // so the no-slash form must also resolve once the directory entry exists.
            URL dirResourceNoSlash = classLoader.getResource("folder");
            assertNotNull(
                    "URLClassLoader must find directory resources without trailing slash",
                    dirResourceNoSlash);
        }
    }

    @Test
    public void testURLClassLoaderCanAccessDirectoryResourcesInResourcesOnlyArchive() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive, ClassLoaderArchiver.getResourceFilter());

        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{archive.toURI().toURL()}, null)) {

            URL fileResource = classLoader.getResource("folder/File.txt");
            assertNotNull("URLClassLoader must find file resources", fileResource);

            URL dirResourceWithSlash = classLoader.getResource("folder/");
            assertNotNull(
                    "URLClassLoader must find directory resources with trailing slash in resources-only archive",
                    dirResourceWithSlash);

            URL dirResourceNoSlash = classLoader.getResource("folder");
            assertNotNull(
                    "URLClassLoader must find directory resources without trailing slash in resources-only archive",
                    dirResourceNoSlash);
        }
    }

    // ─── directory entries present ────────────────────────────────────────────

    @Test
    public void testDirectoryEntriesArePresentInFullArchive() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive);

        Set<String> entries = entryNames(archive);
        assertTrue("Archive must contain a directory entry for 'folder/'",
                entries.contains("folder/"));
    }

    @Test
    public void testDirectoryEntriesArePresentInResourcesOnlyArchive() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive, ClassLoaderArchiver.getResourceFilter());

        Set<String> entries = entryNames(archive);
        assertTrue("Resources-only archive must contain a directory entry for 'folder/'",
                entries.contains("folder/"));
    }

    // ─── resourcesOnly flag ───────────────────────────────────────────────────

    @Test
    public void testResourcesOnlyExcludesClassFiles() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive, ClassLoaderArchiver.getResourceFilter());

        for (String name : entryNames(archive)) {
            assertFalse(".class files must be excluded when resourcesOnly=true: " + name,
                    name.endsWith(".class"));
        }
    }

    @Test
    public void testFullArchiveContainsClassFiles() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive);

        assertTrue("Full archive must contain .class files",
                entryNames(archive).stream().anyMatch(e -> e.endsWith(".class")));
    }

    // ─── manifest ─────────────────────────────────────────────────────────────

    @Test
    public void testArchiveManifestIsMinimal() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive);

        try (JarFile jar = new JarFile(archive)) {
            Manifest manifest = jar.getManifest();
            assertNotNull("Archive must contain a manifest", manifest);
            assertEquals("Manifest-Version must be 1.0",
                    "1.0", manifest.getMainAttributes().getValue("Manifest-Version"));
            assertEquals("Manifest must contain only Manifest-Version (no Main-Class, Class-Path, etc.)",
                    1, manifest.getMainAttributes().size());
        }
    }

    // ─── no duplicate entries ─────────────────────────────────────────────────

    @Test
    public void testNoDuplicateEntries() throws Exception {
        File archive = tmp.newFile("out.jar");
        ClassLoaderArchiver.createArchive(archive);

        Set<String> seen = new HashSet<>();
        try (JarFile jar = new JarFile(archive)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                assertTrue("Duplicate entry found: " + name, seen.add(name));
            }
        }
    }

    // ─── signature file exclusion ─────────────────────────────────────────────

    @Test
    public void testSignatureFilesAreExcluded() throws Exception {
        File signedJar = createJarWithSignatureFiles();
        String originalClasspath = System.getProperty("java.class.path");
        try {
            System.setProperty("java.class.path",
                    originalClasspath + File.pathSeparator + signedJar.getAbsolutePath());

            File archive = tmp.newFile("out.jar");
            ClassLoaderArchiver.createArchive(archive);

            Set<String> entries = entryNames(archive);
            assertTrue("Regular entry from signed JAR must be included",
                    entries.contains("signed-resource.txt"));
            assertFalse(".SF files must be stripped",
                    entries.stream().anyMatch(e -> e.toUpperCase().endsWith(".SF")));
            assertFalse(".RSA files must be stripped",
                    entries.stream().anyMatch(e -> e.toUpperCase().endsWith(".RSA")));
            assertFalse(".DSA files must be stripped",
                    entries.stream().anyMatch(e -> e.toUpperCase().endsWith(".DSA")));
            assertFalse(".EC files must be stripped",
                    entries.stream().anyMatch(e -> e.toUpperCase().endsWith(".EC")));
        } finally {
            System.setProperty("java.class.path", originalClasspath);
        }
    }
}
