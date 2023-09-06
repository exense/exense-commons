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

import ch.exense.commons.processes.ManagedProcess.ManagedProcessException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Test if the Managed process can properly kill child processes
 * Note: this manual test can be heavy on your machine
 */
@Ignore
public class RecursiveProcessTest {

    private static final String NB_PROCESS = "5";
    private static final String DEPTH = "2";

    public static void main(String[] args) throws Exception {
        int nbProcess;
        int depth;

        if (args.length != 2) {
            throw new Exception("Two arguments were expected");
        }
        try {
            nbProcess = Integer.parseInt(args[0]);
            depth = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new Exception("The argument were expected to be integers");
        }

        // start nbProcess child
        if (depth > 0) {
            List<String> argsForChild = new ArrayList<>();
            argsForChild.add(String.valueOf(nbProcess));
            argsForChild.add(String.valueOf(depth - 1));
               
            for (int i = 0; i < nbProcess; i++) {
                ExternalJVMLauncher externalJVMLauncher = new ExternalJVMLauncher("java", Files.createTempDirectory("log_").toFile());
                externalJVMLauncher.launchExternalJVM("MyExternalProcess", RecursiveProcessTest.class, new ArrayList<>(),
                        argsForChild);
            }
        }
        // and wait
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Test
    public void testRecursiveKilling() throws ManagedProcessException, IOException, InterruptedException {

        List<String> args = new ArrayList<>();
        args.add(NB_PROCESS);
        args.add(DEPTH);

        startRandomProcesses();

        ExternalJVMLauncher externalJVMLauncher = new ExternalJVMLauncher("java.exe", new File("log"));
        try (ManagedProcess externalVM = externalJVMLauncher.launchExternalJVM("MyExternalProcess", RecursiveProcessTest.class, new ArrayList<>(), args)) {
            externalVM.waitFor(10000);
        } catch (TimeoutException ignore) {

        }
    }

    private static final int NB_THREADS = 50;

    private void startRandomProcesses() {
        ExecutorService threadPool = Executors.newFixedThreadPool(NB_THREADS);

        for (int i = 0; i < NB_THREADS; i++) {
            threadPool.submit(() -> {
                while (true) {
                    try(ManagedProcess managedProcess = new ManagedProcess("MyJavaProcess", "java -version")) {
                        managedProcess.waitFor(10);
                    } catch (InterruptedException | TimeoutException | ManagedProcessException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}
