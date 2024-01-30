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
import nl.knaw.dans.layerstore.Item;

import java.nio.file.Path;

public class Util {

    /**
     * Convert an Item to a Listing.
     *
     * @param item           the item to convert
     * @param relativeToPath the path to relativize the item path to
     * @return the converted item
     */
    public static Listing fromItem(Item item, String relativeToPath) {
        var itemPath = Path.of(item.getPath());
        var relativeTo = Path.of(relativeToPath);
        var relativePath = relativeTo.relativize(itemPath);

        return switch (item.getType()) {
            case File -> Listing.file(relativePath.toString());
            case Directory -> Listing.directory(relativePath.toString());
        };
    }
}
