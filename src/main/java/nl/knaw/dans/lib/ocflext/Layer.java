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

import io.ocfl.core.storage.common.Listing;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface Layer {

    /**
     * Head layer only.
     *
     * @param path
     */
    void createDirectories(String path) throws LayerNotWritableException, IOException;

    /**
     * Deletes the files pointed to by <code>paths</code>. If the layer is archived, the archive is first unarchived
     * to a staging directory, the files are deleted, and the archive is recreated.
     * @param paths the paths of the files to be deleted
     */
    void deleteFiles(List<String> paths);

    void archive();

    InputStream read(String filePath);
}
