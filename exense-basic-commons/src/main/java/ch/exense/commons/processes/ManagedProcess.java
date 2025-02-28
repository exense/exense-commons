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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a wrapper of {@link Process} which offers additional services
 * like better handling of standard output and standard error output.
 */
public class ManagedProcess implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ManagedProcess.class);

    private static final String DEFAULT_PROCESS_NAME = "ManagedProcess";

    private final ProcessBuilder builder;
    private final String id;
    private final File executionDirectory;
    private final File tempLogDirectory;

    private final boolean redirectOutput;
    private final Map<String, String> environments;

    private Process process;
    private File processOutputLog;
    private File processErrorLog;

    /**
     * @param command the command to be executed (the program and its arguments)
     *                formatted as string
     */
    public ManagedProcess(String command) throws ManagedProcessException {
        this(DEFAULT_PROCESS_NAME, tokenize(command));
    }

    /**
     * @param name    a string describing the process. This is used to prefix the ID
     *                which uniquely identifies the process instance
     * @param command the command to be executed (the program and its arguments)
     *                formatted as string
     */
    public ManagedProcess(String name, String command) throws ManagedProcessException {
        this(name, tokenize(command));
    }

    /**
     * @param commands the list containing the program and its arguments
     */
    public ManagedProcess(List<String> commands) throws ManagedProcessException {
        this(DEFAULT_PROCESS_NAME, commands, null, true);
    }

    /**
     * @param name     a string describing the process. This is used to prefix the ID
     *                 which uniquely identifies the process instance
     * @param commands the list containing the program and its arguments
     */
    public ManagedProcess(String name, List<String> commands) throws ManagedProcessException {
        this(name, commands, null, true);
    }

    /**
     * @param commands     the list containing the program and its arguments
     * @param logDirectory the directory where the logs (stdout and stderr) of the
     *                     process will be written to
     */
    public ManagedProcess(List<String> commands, File logDirectory) throws ManagedProcessException {
        this(DEFAULT_PROCESS_NAME, commands, logDirectory, true);
    }

    public ManagedProcess(String name, List<String> commands, File baseLogDirectory, boolean redirectOutput) throws ManagedProcessException {
        this(name, commands, null, baseLogDirectory, redirectOutput);
    }

    /**
     * @param name               a string describing the process. This is used to prefix
     *                           the ID which uniquely identifies the process instance
     * @param commands           the list containing the program and its arguments
     * @param executionDirectory the directory in which the process will be executed
     */
    public ManagedProcess(String name, List<String> commands, File executionDirectory) {
        this(name, commands, executionDirectory, executionDirectory, true);
    }

    /**
     * @param name               a string describing the process. This is used to prefix
     *                           the ID which uniquely identifies the process instance
     * @param commands           the list containing the program and its arguments
     * @param executionDirectory the directory in which the process will be executed
     * @param baseLogDirectory   the directory where the temporary directory containing the logs (stdout and stderr) of the
     *                           process will be created
     * @param redirectOutput     if the std output of the process should be redirected to a temporary file or kept in the output stream
     */
    public ManagedProcess(String name, List<String> commands, File executionDirectory, File baseLogDirectory, boolean redirectOutput) {
        this(name, commands, executionDirectory, baseLogDirectory, redirectOutput, new HashMap<>());
    }

    /**
     * @param name               a string describing the process. This is used to prefix
     *                           the ID witch uniquely identifies the process instance
     * @param commands           the list containing the program and its arguments
     * @param executionDirectory the directory in which the process will be executed
     * @param baseLogDirectory   the directory where the temporary directory containing the logs (stdout and stderr) of the
     *                           process will be created
     * @param redirectOutput     if the std output of the process should be redirected to a temporary file or kept in the output stream
     * @param environments       list of environment variables to pass to the process
     */
    public ManagedProcess(String name, List<String> commands, File executionDirectory, File baseLogDirectory,
                          boolean redirectOutput, Map<String,String> environments) {
        super();

        this.environments = environments;
        this.redirectOutput = redirectOutput;
        this.id = name + "_" + UUID.randomUUID();
        this.builder = new ProcessBuilder(commands);

        if (baseLogDirectory == null) {
            baseLogDirectory = new File(".");
        }

        this.tempLogDirectory = new File(baseLogDirectory.getAbsolutePath() + "/" + id);

        this.executionDirectory = Objects.requireNonNullElse(executionDirectory, tempLogDirectory);

        createDirectoryIfNotExisting(this.tempLogDirectory);
        createDirectoryIfNotExisting(this.executionDirectory);
    }

    private void createDirectoryIfNotExisting(File directory) {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new InvalidParameterException("Unable to create directory for process " + id
                        + ". Please ensure that the file " + directory.getAbsolutePath() + " is writable.");
            }
        }
    }

    public String getId() {
        return id;
    }

    public OutputStream getProcessOutputStream() {
        return process.getOutputStream();
    }

    public InputStream getProcessInputStream() throws Exception {
        if (redirectOutput) {
            throw new Exception("The process InputStream could not be retrieved if it has been redirected in a file");
        }
        return process.getInputStream();
    }

    public File getProcessOutputLog() {
        return processOutputLog;
    }

    public File getProcessErrorLog() {
        return processErrorLog;
    }

    public String getProcessOutputLogAsString() {
        return readProcessLog(getProcessOutputLog());
    }

    public String getProcessErrorLogAsString() {
        return readProcessLog(getProcessErrorLog());
    }

    /**
     * @return the standard output and error of this process formatted for logging purposes
     */
    public String getProcessLog() {
        return "The output of the process " + id + " was:\n" +
                getProcessOutputLogAsString() +
                "The error output of the process " + id + " was:\n" +
                getProcessErrorLogAsString();
    }

    private static String readProcessLog(File file) {
        if (file != null && file.exists() && file.canRead()) {
            try {
                return Files.readString(file.toPath(), Charset.defaultCharset());
            } catch (IOException e) {
                String errorMessage = "Error while reading process log file " + file.getAbsolutePath();
                logger.error(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        } else {
            return "";
        }
    }

    public File getExecutionDirectory() {
        return executionDirectory;
    }

    private static List<String> tokenize(String command) {
        List<String> tokens = new ArrayList<>();
        Pattern regex = Pattern.compile("[^\\s\"]+|\"([^\"]*)\"");
        Matcher regexMatcher = regex.matcher(command);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                tokens.add(regexMatcher.group(1));
            } else {
                tokens.add(regexMatcher.group());
            }
        }
        return tokens;
    }

    public synchronized void start() throws ManagedProcessException {
        try {
            if (process == null) {
                logger.debug("Starting managed process " + builder.command());
                builder.directory(executionDirectory);

                if (redirectOutput) {
                    processOutputLog = new File(tempLogDirectory + "/ProcessOut.log");
                    builder.redirectOutput(processOutputLog);
                }

                processErrorLog = new File(tempLogDirectory + "/ProcessError.log");
                builder.redirectError(processErrorLog);

                builder.environment().putAll(environments);

                try {
                    process = builder.start();
                } catch (IOException e) {
                    throw new ManagedProcessException("Unable to start the process " + id, e);
                }
                logger.debug("Started managed process " + builder.command());
            } else {
                throw new ManagedProcessException("Unable to start the process " + id + " twice. The process has already been started.");
            }
        } catch (ManagedProcessException e) {
            throw e;
        } catch (Throwable t) {
            try {
                removeTempLogDirectory();
            } finally {
                throw new ManagedProcessException("Unexpected error while starting the process " + id, t);
            }
        }
    }

    /**
     * Start the process and wait until the process has terminated
     *
     * @param timeout the maximum time to wait in ms
     * @return the exit value of the process
     */
    public int startAndWaitFor(long timeout) throws TimeoutException, InterruptedException, ManagedProcessException {
        start();
        return waitFor(timeout);
    }

    /**
     * Causes the current thread to wait, if necessary, until the process has
     * terminated, or the specified waiting time elapses.
     *
     * @param timeout the maximum time to wait in ms
     * @throws TimeoutException     if the process doesn't exit within the defined
     *                              timeout
     * @throws InterruptedException if the thread is interrupted while waiting for
     *                              the process to exit
     */
    public int waitFor(long timeout) throws TimeoutException, InterruptedException {
        boolean terminated = process.waitFor(timeout, TimeUnit.MILLISECONDS);
        if (!terminated) {
            throw new TimeoutException(
                    "The process " + id + " didn't exit within the defined timeout of " + timeout + "ms");
        }
        return process.exitValue();
    }

    public static class ManagedProcessException extends Exception {

        private static final long serialVersionUID = -2205566982535606557L;

        public ManagedProcessException(String message, Throwable cause) {
            super(message, cause);
        }

        public ManagedProcessException(String message) {
            super(message);
        }
    }

    /**
     * Recursively destroy the process without cleaning up the logs and temporary directory
     */
    public void stop() {
        stopProcess(process);
    }

    @Override
    public void close() throws IOException {
        if (logger.isDebugEnabled()) {
            try {
                String errorLog = readProcessLog(getProcessErrorLog());
                logger.debug("Error output from managed process " + id + ": " + errorLog);
                logger.debug("End of error output from managed process " + id);

                if (redirectOutput) {
                    String stdOut = readProcessLog(getProcessOutputLog());
                    logger.debug("Standard output from managed process " + id + ": " + stdOut);
                } else {
                    logger.debug("Standard output from managed process " + id + " was not redirected, nothing to display.");
                }
                logger.debug("End of standard output from managed process " + id);
            } catch (Exception e) {
                logger.error("Error while logging output of process " + id, e);
            }
        }

        if (process != null) {
            stopProcess(process);
            //Close all streams just in case
            try {
                process.getInputStream().close();
            } catch (Exception ignored) {
            }
            try {
                process.getOutputStream().close();
            } catch (Exception ignored) {
            }
            try {
                process.getErrorStream().close();
            } catch (Exception ignored) {
            }
        }
        //the process should be stopped by now
        if (process != null && process.isAlive()) {
            logger.error("Process is still alive");
        }
        removeTempLogDirectory();
    }

    private void stopProcess(Process process) {
        //For process starting child processes there is no guaranty that stopping the parent
        //and waiting on it to finish is sufficient, so stopping all children explicitly
        recursiveStopProcess(process.toHandle());
    }

    private void recursiveStopProcess(ProcessHandle process)  {
        // kill all the children, depth first
        process.children().forEach(this::recursiveStopProcess);
        //Stop process and wait for completion
        process.destroy();
        try {
            int counter=0;
            while (process.isAlive() && counter < 100) {
                counter++;
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void removeTempLogDirectory() {
        Thread thread = new Thread(() -> {
            try {
                FileHelper.deleteFolderWithRetryOnError(tempLogDirectory);
            } catch (Throwable t) {
                logger.error("Unable to delete the managed process temp folder " + tempLogDirectory.getAbsolutePath(), t);
            }
        });
        thread.start();
        try {
            thread.join(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}