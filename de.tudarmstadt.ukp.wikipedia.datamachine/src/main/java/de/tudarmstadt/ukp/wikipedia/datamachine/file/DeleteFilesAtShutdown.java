/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.wikipedia.datamachine.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A file deletion "watch dog" that can be to remove files via its {@link Path} references. It will clean out files
 * upon JVM shutdown: guaranteed!
 * <p>
 * Inspired by and adapted from the answer here:
 * <a href="https://stackoverflow.com/a/42389029">https://stackoverflow.com/a/42389029</a>
 */
public final class DeleteFilesAtShutdown {
    private static Set<Path> paths = new LinkedHashSet<>();

    static {
        // registers the call of 'shutdownHook' at JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(DeleteFilesAtShutdown::cleanupRegisteredFiles));
    }

    private static void cleanupRegisteredFiles() {
        Set<Path> local;
        synchronized(DeleteFilesAtShutdown.class){
            local = paths;
            paths = null;
        }

        List<Path> toBeDeleted = new ArrayList<>(local);
        Collections.reverse(toBeDeleted);
        for (Path p : toBeDeleted) {
            try {
                Files.delete(p);
            } catch (IOException | RuntimeException e) {
                // do nothing - best-effort
            }
        }
    }

    /**
     * Registers a {@link Path} to be removed at JVM shutdown.
     * @param filePath A valid path pointing to a file.
     */
    public static synchronized void register(Path filePath) {
        if (paths == null) {
            throw new IllegalStateException("Shutdown hook is already in progress. Adding paths is not allowed now!");
        }
        paths.add(filePath);
    }
}