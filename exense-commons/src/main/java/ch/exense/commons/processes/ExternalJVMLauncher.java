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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
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
		URL[] urls = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
		StringBuilder cp = new StringBuilder();
		String delimiter = isWindows()?";":":";
		cp.append("\"");
		for(URL url:urls) {
			cp.append(url.getFile()+delimiter);
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
        return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
    }
}
