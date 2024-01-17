/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.lib.ocflext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface Layer {
    void createDirectories(String path) throws IOException;

    /**
     * Deletes the files pointed to by <code>paths</code>. Not allowed when the layer is closed.
     *
     * @param paths the paths of the files to be deleted
     * @throws IOException if the files cannot be deleted
     */
    void deleteFiles(List<String> paths) throws IOException;

    InputStream read(String path) throws IOException;

    /**
     * Changes the state of the layer to closed.
     */
    void close();

    /**
     * Turns the layer into an archive file.
     */
    void archive();

    void write(String filePath, InputStream content) throws IOException;

    void moveDirectoryInto(Path source, String destination) throws IOException;

    boolean fileExists(String path) throws IOException;

    void moveDirectoryInternal(String source, String destination) throws IOException;

    long getId();

    void deleteDirectory(String path) throws IOException;
}
