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
import java.io.OutputStream;
import java.util.List;

/**
 * Provides methods for managing layers that are part of a layered storage. The layers are ordered from top to bottom, where the top layer is the most recent layer. Only the top layer is writable. The
 * other layers are read-only, except that files can be deleted from them.
 */
public interface LayerManager {
    /**
     * Closes the current top layer and creates a new top layer. The old top layer becomes read-only and will be closed. The closing does <em>not</em> have to be done synchronously.
     *
     */
    void openNewTopLayer();


    /**
     * Creates the directories pointed to by <code>path</code>. The directories are created in the top layer.
     *
     * @param path the path of the directories to be created
     * @throws LayerNotWritableException if the layer is not writable, i.e. if it is not the top layer
     * @throws IOException               if an I/O error occurs
     */
    void createDirectories(String path) throws LayerNotWritableException, IOException;

    /**
     * Writes the given content to the file pointed to by <code>filePath</code>. The file is created in the top layer.
     *
     * @param filePath  the path of the file to be written
     * @param content   the content to be written
     * @throws LayerNotWritableException if the layer is not writable, i.e. if it is not the top layer
     * @throws IOException               if an I/O error occurs
     */
//    void write(String filePath, byte[] content, String mediaType) throws LayerNotWritableException, IOException;


    void write(String filePath, InputStream content) throws LayerNotWritableException, IOException;


    InputStream read(String filePath) throws IOException;

    /**
     * Finds all the layers that contain the given path.
     *
     * @param path path relative to the root of the storage
     * @return the layers that contain the given path
     */
    List<Layer> findLayersContaining(String path);

    List<Listing> listDirectory(String directoryPath);

    List<Listing> listRecursive(String directoryPath);

    boolean fileExists(String filePath);

}
