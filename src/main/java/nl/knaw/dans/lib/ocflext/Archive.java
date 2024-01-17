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

/**
 * An archive is a file that contains a collection of files and directories. It can be implemented as a zip file, a tar, etc.
 */
public interface Archive {

    /**
     * Returns an input stream for the file at the given path. The caller is responsible for closing the stream.
     *
     * @param filePath the path of the file to read
     * @return the input stream
     * @throws IOException if an I/O error occurs
     */
    InputStream getInputStreamFor(String filePath) throws IOException;

    /**
     * Unarchives the archive to the given staging directory.
     *
     * @param stagingDir the directory to unarchive to
     */
    void unarchiveTo(Path stagingDir);

    /**
     * Archives the given staging directory overwriting the backing file, if it exists.
     *
     * @param stagingDir the directory to archive
     */
    void archiveFrom(Path stagingDir);

    /**
     * Returns whether the archive exists.
     *
     * @return whether the archive exists
     */
    boolean isArchived();


}
