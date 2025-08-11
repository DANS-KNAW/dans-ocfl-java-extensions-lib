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

import nl.knaw.dans.layerstore.DatabaseBackedContentManager;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Content manager that selects inventory and sidecar files as well as files in extension directories for storage in the database, but not those in the content directory, as it is theoretically
 * possible to have inventory and sidecar files as part of the payload. It also compresses and decompresses the content of the inventory files, as these are likely to grow large. The sidecar files
 * are not compressed.
 */
public class StoreInventoryDbBackedContentManager implements DatabaseBackedContentManager {
    // inventory.json.* is a sidecar file
    private final Pattern sidecarPattern = Pattern.compile("^inventory.json\\..+$");

    @Override
    public boolean test(String s) {
        var path = Path.of(s);
        return isInventoryJson(path) || isSidecar(path) || isFileInExtensionsDir(path);
    }

    private boolean isInventoryJson(Path path) {
        return path.getFileName().toString().equals("inventory.json") && isOutsideContentDirectory(path);
    }

    private boolean isSidecar(Path path) {
        return sidecarPattern.matcher(path.getFileName().toString()).matches() && isOutsideContentDirectory(path);
    }

    private boolean isOutsideContentDirectory(Path path) {
        return Stream.iterate(path.getParent(), Objects::nonNull, Path::getParent)
            .noneMatch(parent -> parent.getFileName().toString().equals("content"));
    }

    private boolean isFileInExtensionsDir(Path path) {
        return isOutsideContentDirectory(path) && Stream.iterate(path.getParent(), Objects::nonNull, Path::getParent)
            .anyMatch(parent -> parent.getFileName().toString().equals("extensions"));
    }

    @Override
    public byte[] preStore(String path, byte[] bytes) {
        if (isSidecar(Path.of(path))) {
            return bytes;
        }
        else {
            try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                GzipCompressorOutputStream gzipStream = new GzipCompressorOutputStream(byteStream)) {
                gzipStream.write(bytes);
                gzipStream.close();
                return byteStream.toByteArray();
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to compress data", e);
            }
        }
    }

    @Override
    public byte[] postRetrieve(String path, byte[] bytes) {
        if (isSidecar(Path.of(path))) {
            return bytes;
        }
        else {
            try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
                GzipCompressorInputStream gzipStream = new GzipCompressorInputStream(byteStream);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                return outStream.toByteArray();
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to decompress data", e);
            }
        }
    }
}
