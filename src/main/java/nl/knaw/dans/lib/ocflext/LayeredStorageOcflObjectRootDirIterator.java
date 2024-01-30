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
import io.ocfl.core.storage.common.OcflObjectRootDirIterator;
import lombok.AllArgsConstructor;
import nl.knaw.dans.layerstore.Item;
import nl.knaw.dans.layerstore.ItemStore;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static io.ocfl.api.OcflConstants.OBJECT_NAMASTE_PREFIX;

@AllArgsConstructor
public class LayeredStorageOcflObjectRootDirIterator extends OcflObjectRootDirIterator {
    private final ItemStore itemStore;

    @Override
    protected boolean isObjectRoot(String path) {
        return itemStore.existsPathLike(path + "/" + OBJECT_NAMASTE_PREFIX + "%");
    }

    @Override
    protected Directory createDirectory(String path) {
        return new LayeredStorageDirectory(path);
    }

    private class LayeredStorageDirectory implements Directory {
        private final Iterator<String> childDirectoryIterator;

        LayeredStorageDirectory(String path) {
            try {
                var items = itemStore.listDirectory(path);
                List<String> childDirectories = items.stream()
                    .filter(item -> item.getType() == Item.Type.Directory)
                    .map(Item::getPath)
                    .toList();
                childDirectoryIterator = childDirectories.iterator();
            } catch (IOException e) {
                throw OcflIOException.from(e);
            }
        }

        @Override
        public String nextChildDirectory() {
            if (childDirectoryIterator.hasNext()) {
                return childDirectoryIterator.next();
            }
            else {
                return null;
            }
        }

        @Override
        public void close() throws IOException {

        }
    }
}
