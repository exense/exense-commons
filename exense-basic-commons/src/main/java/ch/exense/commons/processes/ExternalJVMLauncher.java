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
import java.util.ArrayList;
import java.util.List;

import ch.exense.commons.processes.ManagedProcess.ManagedProcessException;

public class ExternalJVMLauncher {

	private final String javaPath;
	
	private final File processLogFolder;
	
	public ExternalJVMLauncher(String javaPath, File processLogFolder) {
		super();
		this.javaPath = javaPath;
		this.processLogFolder = processLogFolder;
	}

	private String buildClasspath() {
		//Fix for Java11 compatibility (Classloader is no more an instance of URLClassLoader)
		String[] classPathEntries = System.getProperty("java.class.path").split(File.pathSeparator);

		StringBuilder cp = new StringBuilder();
		cp.append("\"");
		for(String path:classPathEntries) {
			cp.append(new File(path).getAbsolutePath()+File.pathSeparator);
		}
		cp.append("\"");
		return cp.toString();
	}

	public ManagedProcess launchExternalJVM(String name, Class<?> mainClass, List<String> vmargs, List<String> progargs) throws ManagedProcessException {
		return launchExternalJVM(name, mainClass, vmargs, progargs, true);
	}
	
	public ManagedProcess launchExternalJVM(String name, Class<?> mainClass, List<String> vmargs, List<String> progargs, boolean redirectOutput) throws ManagedProcessException {
		String cp = buildClasspath();
		
		List<String> cmd = new ArrayList<>();
		cmd.add(javaPath);
		cmd.add("-cp");
		cmd.add(cp);
		
		cmd.addAll(vmargs);
		
		cmd.add(mainClass.getName());
		
		cmd.addAll(progargs);
		
		ManagedProcess process = new ManagedProcess(name, cmd, processLogFolder, redirectOutput);
		process.start();
		return process;
	}
	public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
