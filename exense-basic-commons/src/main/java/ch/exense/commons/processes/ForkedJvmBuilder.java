package ch.exense.commons.processes;

import ch.exense.commons.io.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ForkedJvmBuilder implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ForkedJvmBuilder.class);

    private final File cpArgFile;
    private final List<String> command = new ArrayList<>();

    public ForkedJvmBuilder(String javaPath, String mainClass, List<String> vmargs, List<String> progargs) throws IOException {
        cpArgFile = buildClasspath();
        command.add(javaPath);
        command.add("@" + cpArgFile.getCanonicalPath());
        command.addAll(vmargs);
        command.add(mainClass);
        command.addAll(progargs);
    }

    public List<String> getProcessCommand() {
        return command;
    }

    protected static File buildClasspath() throws IOException {
        //Fix for Java11 compatibility (Classloader is no more an instance of URLClassLoader)
        String[] classPathEntries = System.getProperty("java.class.path").split(File.pathSeparator);

        File javaClassPathArgsFile = FileHelper.createTempFile();

        try (OutputStreamWriter cp = new OutputStreamWriter(new FileOutputStream(javaClassPathArgsFile), StandardCharsets.UTF_8)) {
            cp.append("-cp ");
            cp.append("\"");
            for(String path:classPathEntries) {
                String absolutePath = new File(path).getCanonicalPath();
                absolutePath = (isWindows()) ? absolutePath.replace("\\","/") : absolutePath;
                cp.append(absolutePath);
                cp.append(File.pathSeparator);
            }
            cp.append("\"");
        }
        return javaClassPathArgsFile;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Override
    public void close() {
        if (cpArgFile != null && cpArgFile.exists()) {
            boolean delete = cpArgFile.delete();
            if (!delete) {
                logger.error("Unable to delete temporary file: {}", cpArgFile.getAbsolutePath());
            }
        }
    }
}
