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
import java.util.List;

/**
 * Provides access to the database that contains a record for every file and directory stored in the layered storage. Contains methods to add, delete and find information about these files and
 * directories.
 */
public interface LayerDatabase {

    /**
     * Lists the items in <code>directoryPath</code>. It takes into account the complete stack of layers. If the directory does not exist in any of the layers, an IllegalArgumentException is thrown.
     *
     * @param directoryPath the directory path relative to the storage root
     * @return the items in the directory
     */
    List<ListingRecord> listDirectory(String directoryPath) throws IOException;

    /**
     * Lists the items in <code>directoryPath</code> recursively. It takes into account the complete stack of layers.
     *
     * @param directoryPath the directory path relative to the storage root
     * @return the items in the directory and its subdirectories
     */
    List<ListingRecord> listRecursive(String directoryPath) throws IOException;

    /**
     * Adds a directory to the database. Ancestor directories are added automatically, if they do not exist in the same layer yet.
     *
     * @param layerId the id of the layer that contains the directory
     * @param path    the path of the directory relative to the storage root
     * @return
     */
    List<ListingRecord> addDirectories(long layerId, String path);

    void saveRecords(List<ListingRecord> records);

    /**
     * Adds a file to the database. The record for the parent directory must already exist.
     *
     * @param layerId  the id of the layer that contains the file
     * @param filePath the path of the file relative to the storage root
     */
    void addFile(long layerId, String filePath);

    void deleteRecords(List<ListingRecord> records);

    /**
     * Finds all the layers that contain the given path.
     *
     * @param path path relative to the root of the storage
     * @return the ids of the layers that contain the path
     */
    List<Long> findLayersContaining(String path);

    /**
     * Returns whether the file content is stored in the database for the newest layer that contains it.
     *
     * @param filePath path relative to the root of the storage
     * @return true if the content is stored in the database, false otherwise
     */
    boolean isContentStoredInDatabase(String filePath);

    /**
     * Reads the file from the database from the most recent layer that contains it.
     *
     * @param filePath path relative to the root of the storage
     * @return the input stream
     */
    byte[] readContentFromDatabase(String filePath);
}
