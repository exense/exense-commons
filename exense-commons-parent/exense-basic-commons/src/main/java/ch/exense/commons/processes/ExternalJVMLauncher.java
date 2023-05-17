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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ch.exense.commons.io.FileHelper;
import ch.exense.commons.processes.ManagedProcess.ManagedProcessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalJVMLauncher {

	private static final Logger logger = LoggerFactory.getLogger(ExternalJVMLauncher.class);
	private final String javaPath;
	
	private final File processLogFolder;

	public ExternalJVMLauncher(String javaPath, File processLogFolder) {
		super();
		this.javaPath = javaPath;
		this.processLogFolder = processLogFolder;
	}

	private File buildClasspath() throws IOException {
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

	public ManagedProcess launchExternalJVM(String name, Class<?> mainClass, List<String> vmargs, List<String> progargs) throws ManagedProcessException {
		return launchExternalJVM(name, mainClass, vmargs, progargs, true);
	}
	
	public ManagedProcess launchExternalJVM(String name, Class<?> mainClass, List<String> vmargs, List<String> progargs, boolean redirectOutput) throws ManagedProcessException {
		try {
			File cpArgFile = buildClasspath();
			List<String> cmd = new ArrayList<>();
			cmd.add(javaPath);
			cmd.add("@" + cpArgFile.getCanonicalPath());

			cmd.addAll(vmargs);

			cmd.add(mainClass.getName());

			cmd.addAll(progargs);

			ManagedProcess process = new JvmManagedProcess(cpArgFile, name, cmd, processLogFolder, redirectOutput);
			process.start();
			return process;
		} catch (IOException e) {
			throw new ManagedProcessException("Unable to create the java argument file specifying the classpath.",e);
		}
	}
	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	private static class JvmManagedProcess extends ManagedProcess {

		private File javaClassPathArgsFile;

		public JvmManagedProcess(File javaClassPathArgsFile, String name, List<String> commands, File baseLogDirectory, boolean redirectOuput) throws ManagedProcessException {
			super(name, commands, baseLogDirectory, redirectOuput);
			this.javaClassPathArgsFile = javaClassPathArgsFile;
		}

		@Override
		public void close() throws IOException {
			super.close();
			if (javaClassPathArgsFile != null && javaClassPathArgsFile.exists()) {
				boolean delete = javaClassPathArgsFile.delete();
				if (!delete) {
					logger.error("Unable to delete temporary file: " + javaClassPathArgsFile.getCanonicalPath());
				}
			}
		}
	}
}
