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

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.ocfl.api.OcflConstants.OBJECT_NAMASTE_PREFIX;

@AllArgsConstructor
public class InventoryFilter implements Filter<Path> {

    @Override
    public boolean accept(Path path) {
        return (isOcflObjectRoot(path.getParent())) && path.getFileName().toString().equals("inventory.json"); // TODO: also accept inventory.json.sha512 etc
    }

    @SneakyThrows
    private boolean isOcflObjectRoot(Path path) {
        try (var objectMarkers = Files.newDirectoryStream(path, InventoryFilter::isNamaste)) {
            return objectMarkers.iterator().hasNext();
        }
    }

    private static boolean isNamaste(Path p) {
        return p.getFileName().toString().startsWith(OBJECT_NAMASTE_PREFIX);
    }
}
