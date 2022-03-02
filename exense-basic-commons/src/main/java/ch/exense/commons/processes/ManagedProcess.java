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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.exense.commons.io.FileHelper;

/**
 * This class is a wrapper of {@link Process} which offers additional services
 * like better handling of standard output and standard error output.
 * 
 */
public class ManagedProcess implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(ManagedProcess.class);

	private static final String DEFAULT_PROCESS_NAME = "ManagedProcess";

	private final ProcessBuilder builder;
	private final String id;
	private final File executionDirectory;
	
	private boolean redirectOuput;

	private Process process;
	private File processOutputLog;
	private File processErrorLog;
	
	/**
	 * @param command the command to be executed (the program and its arguments)
	 *                formatted as string
	 * @throws ManagedProcessException
	 */
	public ManagedProcess(String command) throws ManagedProcessException {
		this(DEFAULT_PROCESS_NAME, tokenize(command));
	}

	/**
	 * @param name    a string describing the process. This is used to prefix the ID
	 *                which uniquely identifies the process instance
	 * @param command the command to be executed (the program and its arguments)
	 *                formatted as string
	 * @throws ManagedProcessException
	 */
	public ManagedProcess(String name, String command) throws ManagedProcessException {
		this(name, tokenize(command));
	}

	/**
	 * @param commands the list containing the program and its arguments
	 * @throws ManagedProcessException
	 */
	public ManagedProcess(List<String> commands) throws ManagedProcessException {
		this(DEFAULT_PROCESS_NAME, commands, null, true);
	}

	/**
	 * @param name    a string describing the process. This is used to prefix the ID
	 *                which uniquely identifies the process instance
	 * @param commands the list containing the program and its arguments
	 * @throws ManagedProcessException
	 */
	public ManagedProcess(String name, List<String> commands) throws ManagedProcessException {
		this(name, commands, null, true);
	}

	/**
	 * @param commands      the list containing the program and its arguments
	 * @param logDirectory the directory where the logs (stdout and stderr) of the
	 *                     process will be written to
	 * @throws ManagedProcessException
	 */
	public ManagedProcess(List<String> commands, File logDirectory) throws ManagedProcessException {
		this(DEFAULT_PROCESS_NAME, commands, logDirectory,true);
	}

	/**
	 * @param name         a string describing the process. This is used to prefix
	 *                     the ID wich uniquely identifies the process instance
	 * @param commands      the list containing the program and its arguments
	 * @param logDirectory the directory where the logs (stdout and stderr) of the
	 *                     process will be written to
	 * @throws ManagedProcessException
	 */
	public ManagedProcess(String name, List<String> commands, File logDirectory, boolean redirectOuput) throws ManagedProcessException {
		super();

		this.redirectOuput = redirectOuput;
		
		if (logDirectory == null) {
			logDirectory = new File(".");
		}

		UUID uuid = UUID.randomUUID();
		this.id = name + "_" + uuid;
		builder = new ProcessBuilder(commands);

		executionDirectory = new File(logDirectory.getAbsolutePath() + "/" + id);
		if (!executionDirectory.exists()) {
			if (!executionDirectory.mkdirs()) {
				throw new InvalidParameterException("Unable to create log folder for process " + id
						+ ". Please ensure that the folder " + logDirectory.getAbsolutePath() + " exists and is writable.");
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
		if (redirectOuput) {
			throw new Exception("The process InputStream could not be retrived if it has been redirected in a file");
		}
		return process.getInputStream();
	}

	public File getProcessOutputLog() {
		return processOutputLog;
	}

	public File getProcessErrorLog() {
		return processErrorLog;
	}

	public File getExecutionDirectory() {
		return executionDirectory;
	}

	private static List<String> tokenize(String command) {
		List<String> tokens = new ArrayList<String>();
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

	public void start() throws ManagedProcessException {
		synchronized (this) {
			try {
				if (process == null) {
					logger.debug("Starting managed process " + builder.command());
					builder.directory(executionDirectory);

					if (redirectOuput) {
						processOutputLog = new File(executionDirectory + "/ProcessOut.log");
						builder.redirectOutput(processOutputLog);
					}

					processErrorLog = new File(executionDirectory + "/ProcessError.log");
					builder.redirectError(processErrorLog);
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
				removeFolder();
			}
		}
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

	public class ManagedProcessException extends Exception {

		private static final long serialVersionUID = -2205566982535606557L;

		public ManagedProcessException(String message, Throwable cause) {
			super(message, cause);
		}

		public ManagedProcessException(String message) {
			super(message);
		}
	}

	@Override
	public void close() throws IOException {
        if (logger.isDebugEnabled()) {
            try {
                String errorLog = new String(Files.readAllBytes(getProcessErrorLog().toPath()), Charset.defaultCharset());
                logger.debug("Error output from managed process " + id + ": " + errorLog);
                logger.debug("End of error output from managed process " + id);

                if (redirectOuput) {
                    String stdOut = new String(Files.readAllBytes(getProcessOutputLog().toPath()), Charset.defaultCharset());
                    logger.debug("Standard output from managed process " + id + ": " + stdOut);
                } else {
                    logger.debug("Standard output from managed process " + id + " was not redirected, nothing to display.");
                }
                logger.debug("End of standard output from managed process " + id);
            } catch (IOException e) {
                logger.error("Error while logging output of process " + id, e);
            }
        }

        if (process != null) {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
            }
        }
		removeFolder();
    }

    private void removeFolder() {
		try {
			FileHelper.deleteFolder(executionDirectory);
		} catch (Throwable t) {}
	}

}
