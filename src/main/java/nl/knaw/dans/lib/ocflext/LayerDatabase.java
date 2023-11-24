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
import java.util.Set;

public interface LayerDatabase {

    Set<Listing> listDirectory(String directoryPath);

    Set<Listing> listRecursive(String directoryPath);

    void addDirectory(long layerId, String path);

    void addFile(long layerId, String filePath);

    void delete(long layerId, String path);
}
