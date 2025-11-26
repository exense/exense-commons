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
import java.io.IOException;
import java.util.List;

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

    public ManagedProcess launchExternalJVM(String name, Class<?> mainClass, List<String> vmargs, List<String> progargs) throws ManagedProcessException {
        return launchExternalJVM(name, mainClass, vmargs, progargs, true);
    }

    public ManagedProcess launchExternalJVM(String name, Class<?> mainClass, List<String> vmargs, List<String> progargs, boolean redirectOutput) throws ManagedProcessException {
        return launchExternalJVM(name, mainClass.getName(), vmargs, progargs, redirectOutput);
    }

    public ManagedProcess launchExternalJVM(String name, String mainClass, List<String> vmargs, List<String> progargs, boolean redirectOutput) throws ManagedProcessException {
        try {
            ForkedJvmBuilder forkedJvmBuilder = new ForkedJvmBuilder(javaPath, mainClass, vmargs, progargs);
            ManagedProcess process = new JvmManagedProcess(forkedJvmBuilder, name, processLogFolder, redirectOutput);
            process.start();
            return process;
        } catch (IOException e) {
            throw new ManagedProcessException("Unable to create the java argument file specifying the classpath.", e);
        }
    }

    private static class JvmManagedProcess extends ManagedProcess {

        private final ForkedJvmBuilder forkedJvmBuilder;

        public JvmManagedProcess(ForkedJvmBuilder result, String name, File baseLogDirectory, boolean redirectOutput) throws ManagedProcessException {
            super(name, result.getProcessCommand(), baseLogDirectory, redirectOutput);
            this.forkedJvmBuilder = result;
        }

        @Override
        public void close() throws IOException {
            super.close();
            forkedJvmBuilder.close();
        }
    }
}
