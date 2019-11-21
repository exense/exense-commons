/*******************************************************************************
 * (C) Copyright 2018 Jerome Comte and Dorian Cransac
 *
 * This file is part of exense Commons
 *
 * exense Commons is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * exense Commons is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with exense Commons.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
 * @author jcomte
 *
 */
public class ManagedProcess implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(ManagedProcess.class);

	private static final String DEFAULT_PROCESS_NAME = "ManagedProcess";

	private final ProcessBuilder builder;
	private final String id;
	private final File executionDirectory;

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
		this(DEFAULT_PROCESS_NAME, commands, null);
	}

	/**
	 * @param name    a string describing the process. This is used to prefix the ID
	 *                which uniquely identifies the process instance
	 * @param commands the list containing the program and its arguments
	 * @throws ManagedProcessException
	 */
	public ManagedProcess(String name, List<String> commands) throws ManagedProcessException {
		this(name, commands, null);
	}

	/**
	 * @param commands      the list containing the program and its arguments
	 * @param logDirectory the directory where the logs (stdout and stderr) of the
	 *                     process will be written to
	 * @throws ManagedProcessException
	 */
	public ManagedProcess(List<String> commands, File logDirectory) throws ManagedProcessException {
		this(DEFAULT_PROCESS_NAME, commands, logDirectory);
	}

	/**
	 * @param name         a string describing the process. This is used to prefix
	 *                     the ID wich uniquely identifies the process instance
	 * @param commands      the list containing the program and its arguments
	 * @param logDirectory the directory where the logs (stdout and stderr) of the
	 *                     process will be written to
	 * @throws ManagedProcessException
	 */
	public ManagedProcess(String name, List<String> commands, File logDirectory) throws ManagedProcessException {
		super();

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

	public InputStream getProcessInputStream() {
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
			if (process == null) {
				logger.debug("Starting managed process " + builder.command());
				builder.directory(executionDirectory);
				processOutputLog = new File(executionDirectory + "/ProcessOut.log");
				builder.redirectOutput(processOutputLog);
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
		if(logger.isDebugEnabled()) {
			try {
				String errorLog = new String(Files.readAllBytes(getProcessErrorLog().toPath()), Charset.defaultCharset());
				logger.debug("Error output from managed process "+id+": " + errorLog);
				logger.debug("End of error output from managed process "+id);
				
				String stdOut = new String(Files.readAllBytes(getProcessOutputLog().toPath()), Charset.defaultCharset());
				logger.debug("Standard output from managed process "+id+": " + stdOut);
				logger.debug("End of standard output from managed process "+id);
			} catch (IOException e) {
				logger.error("Error while logging output of process "+id, e);
			}
		}
		
		if (process != null) {
			process.destroy();
			try {
				process.waitFor();
			} catch (InterruptedException e) {
			}
		}
		FileHelper.deleteFolder(executionDirectory);
	}

}
