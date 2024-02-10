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

import nl.knaw.dans.layerstore.Filter;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Filter that selects inventory.json and sidecar files, but not those in the content directory, as it is theoretically possible to have inventory.json and sidecar files as part of the payload.
 */
public class InventoryFilter implements Filter<String> {
    // inventory.json.* is a sidecar file
    private final Pattern sidecarPattern = Pattern.compile("^inventory.json\\..+$");

    @Override
    public boolean accept(String s) {
        var path = Path.of(s);
        return (path.getFileName().toString().equals("inventory.json") || sidecarPattern.matcher(path.getFileName().toString()).matches())
            && !hasContentDirAncestor(path);
    }

    private boolean hasContentDirAncestor(Path path) {
        return Stream.iterate(path.getParent(), Objects::nonNull, Path::getParent)
            .anyMatch(parent -> parent.getFileName().toString().equals("content"));
    }
}
