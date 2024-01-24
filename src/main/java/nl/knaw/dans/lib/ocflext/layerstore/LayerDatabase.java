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
package nl.knaw.dans.lib.ocflext.layerstore;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides access to the database that contains a record for every file and directory stored in the layered storage. Contains methods to add, delete and find information about these files and
 * directories.
 */
public interface LayerDatabase {

    /**
     * Save records to the database. The records may be new or existing. If a record already exists, it is updated.
     *
     * @param records the records to save
     */
    void saveRecords(ListingRecord2... records);

    /**
     * Deletes the records with the given ids.
     *
     * @param ids the ids of the records to delete
     */
    void deleteRecordsById(long... ids);

    /**
     * Retrieves a stream of all the records in the database.
     *
     * @return a list of all the records in the database
     */
    Stream<ListingRecord2> getAllRecords();


    /**
     * Lists the items in <code>directoryPath</code>. It takes into account the complete stack of layers. If the directory does not exist in any of the layers, an IllegalArgumentException is thrown.
     *
     * @param directoryPath the directory path relative to the storage root
     * @return the items in the directory
     */
    List<ListingRecord2> listDirectory(String directoryPath) throws IOException;

    /**
     * Lists the items in <code>directoryPath</code> recursively. It takes into account the complete stack of layers.
     *
     * @param directoryPath the directory path relative to the storage root
     * @return the items in the directory and its subdirectories
     */
    List<ListingRecord2> listRecursive(String directoryPath) throws IOException;

    /**
     * Adds a directory to the database. Ancestor directories are added automatically, if they do not exist in the same layer yet.
     * The directory is always created in the newest layer.
     *
     * @param path    the path of the directory relative to the storage root
     * @return the records that were added to the database for directories that did not exist yet
     */
    List<ListingRecord2> addDirectory(String path);

    /**
     * Finds all the layers that contain the given path.
     *
     * @param path path relative to the root of the storage
     * @return the ids of the layers that contain the path
     */
    List<Long> findLayersContaining(String path);

    /**
     * Returns the records for the given path in any layer. The records are ordered by layer id, with the newest layer first.
     *
     * @param path path relative to the root of the storage
     * @return the records for the given path
     */
    List<ListingRecord2> getRecordsByPath(String path);


    /**
     * Returns whether the path pattern matches any path in the database.
     *
     * @param pathPattern a path pattern that may contain wildcards
     * @return true if the pattern matches any path in the database, false otherwise
     */
    boolean hasPathLike(String pathPattern);
}
