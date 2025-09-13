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
import java.util.regex.Pattern;

/**
 * A {@link DatabaseBackedContentManager} that selects
 */
public class StoreInventoryDbBackedContentManager implements DatabaseBackedContentManager {
    // inventory.json.* is a sidecar file
    private final Pattern sidecarPattern = Pattern.compile("^inventory.json\\..+$");

    @Override
    public boolean test(String s) {
        var path = Path.of(s);
        return !isInsideOcflObjectVersionContentDirectory(path) && !isInMutableHead(path);
    }

    private boolean isInMutableHead(Path path) {
        Path extensionsDir = findTopmostParentNamed(path, "extensions");
        if (extensionsDir == null) {
            return false;
        }
        return path.startsWith(extensionsDir.resolve("mutable-head"));
    }

    private boolean isSidecar(Path path) {
        return sidecarPattern.matcher(path.getFileName().toString()).matches() && isInsideOcflObjectVersionContentDirectory(path);
    }

    private boolean isInsideOcflObjectVersionContentDirectory(Path path) {
        Path contentDir = findTopmostParentNamed(path, "content");
        if (contentDir == null) {
            return false;
        }
        return findTopmostParentNamed(contentDir, "extensions") == null;
    }

    private Path findTopmostParentNamed(Path path, String name) {
        Path result = null;
        for (Path p = path; p != null; p = p.getParent()) {
            if (p.getFileName() != null && p.getFileName().toString().equals(name)) {
                result = p;
            }
        }
        return result;
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
