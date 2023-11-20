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

import io.ocfl.api.exception.OcflIOException;
import io.ocfl.core.storage.common.Listing;
import io.ocfl.core.storage.common.OcflObjectRootDirIterator;
import io.ocfl.core.util.FileUtil;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Collectors;

import static io.ocfl.api.OcflConstants.OBJECT_NAMASTE_PREFIX;

@AllArgsConstructor
public class LayeredOcflObjectRootDirIterator extends OcflObjectRootDirIterator {
    private final LayerManager layerManager;

    @Override
    protected boolean isObjectRoot(String path) {
        if (layerManager.fileExists(path)) {
            return layerManager.listDirectory(path).stream().anyMatch(LayeredOcflObjectRootDirIterator::isNamaste);
        }
        else {
            return false; // TODO: or throw exception?
        }
    }

    private static boolean isNamaste(Listing listing) {
        return listing.isFile() && Paths.get(listing.getRelativePath()).getFileName().toString().startsWith(OBJECT_NAMASTE_PREFIX);
    }

    @Override
    protected Directory createDirectory(String path) {
        // object pushed on a stack by hasNext
        return new LayeredStorageDirectory(path);
    }

    private class LayeredStorageDirectory implements Directory {

        private final DirectoryStream<Path> stream;
        private final Iterator<Path> children;

        LayeredStorageDirectory(String path) {
            stream = layerManager.listDirectory(path)
                .stream()
                .map(Listing::getRelativePath)
                .map(Paths::get).
        }

        @Override
        public String nextChildDirectory() {
            while (children.hasNext()) {
                var child = children.next();

                if (Files.exists(child, LinkOption.NOFOLLOW_LINKS)) {
                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        return FileUtil.pathToStringStandardSeparator(stagingDir.relativize(child));
                    }
                }
                else {
                    var listings = database.getListingsByPath(child.toString());
                    if (!listings.isEmpty() && listings.get(0).isDirectory()) {
                        return FileUtil.pathToStringStandardSeparator(child);
                    }
                }
            }
            return null;
        }

        @Override
        public void close() {
            try {
                stream.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
    }

}
