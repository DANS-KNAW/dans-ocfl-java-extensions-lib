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

import io.ocfl.api.OcflFileRetriever;
import io.ocfl.api.model.DigestAlgorithm;
import io.ocfl.core.storage.common.Listing;
import io.ocfl.core.storage.common.OcflObjectRootDirIterator;
import io.ocfl.core.storage.common.Storage;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class LayeredStorage implements Storage {
    @Override
    public List<Listing> listDirectory(String directoryPath) {
        return null;
    }

    @Override
    public List<Listing> listRecursive(String directoryPath) {
        return null;
    }

    @Override
    public boolean directoryIsEmpty(String directoryPath) {
        return false;
    }

    @Override
    public OcflObjectRootDirIterator iterateObjects() {
        return null;
    }

    @Override
    public boolean fileExists(String filePath) {
        return false;
    }

    @Override
    public InputStream read(String filePath) {
        return null;
    }

    @Override
    public String readToString(String filePath) {
        return null;
    }

    @Override
    public OcflFileRetriever readLazy(String filePath, DigestAlgorithm algorithm, String digest) {
        return null;
    }

    @Override
    public void write(String filePath, byte[] content, String mediaType) {

    }

    @Override
    public void createDirectories(String path) {

    }

    @Override
    public void copyDirectoryOutOf(String source, Path destination) {

    }

    @Override
    public void copyFileInto(Path source, String destination, String mediaType) {

    }

    @Override
    public void copyFileInternal(String sourceFile, String destinationFile) {

    }

    @Override
    public void moveDirectoryInto(Path source, String destination) {

    }

    @Override
    public void moveDirectoryInternal(String source, String destination) {

    }

    @Override
    public void deleteDirectory(String path) {

    }

    @Override
    public void deleteFile(String path) {

    }

    @Override
    public void deleteFiles(Collection<String> paths) {

    }

    @Override
    public void deleteEmptyDirsDown(String path) {

    }

    @Override
    public void deleteEmptyDirsUp(String path) {

    }
}
