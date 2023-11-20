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

import java.util.List;

/**
 * Provides methods for managing layers that are part of a layered storage. The layers are ordered from top to bottom, where the top layer is the most recent layer.
 * Only the top layer is writable. The other layers are read-only, except that files can be deleted from them.
 */
public interface LayerManager {

    /**
     * @return the top layer
     */
    Layer getTopLayer();

    /**
     * Archives the current top layer and creates a new top layer.
     */
    void newTopLayer();


    Layer findHighestLayerContaining(String path);

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
