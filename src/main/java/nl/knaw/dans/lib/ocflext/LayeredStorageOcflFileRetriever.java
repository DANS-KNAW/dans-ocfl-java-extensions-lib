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
import io.ocfl.api.exception.OcflIOException;
import io.ocfl.api.io.FixityCheckInputStream;
import io.ocfl.api.model.DigestAlgorithm;
import lombok.AllArgsConstructor;
import nl.knaw.dans.layerstore.ItemStore;
import org.apache.commons.io.input.BoundedInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * OcflFileRetriever that lazily reads files from a layered ItemStore using logical paths.
 */
@AllArgsConstructor
public class LayeredStorageOcflFileRetriever implements OcflFileRetriever {
    private final ItemStore itemStore;
    private final String filePath;
    private final DigestAlgorithm digestAlgorithm;
    private final String digestValue;

    @Override
    public FixityCheckInputStream retrieveFile() {
        try {
            return new FixityCheckInputStream(
                new BufferedInputStream(itemStore.readFile(filePath)),
                digestAlgorithm,
                digestValue);
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }

    /**
     * Returns a range of bytes from the file. Both {@code startPosition} and {@code endPosition} are
     * inclusive byte offsets. A {@code null} startPosition is treated as 0; a {@code null} endPosition
     * reads until EOF.
     */
    @Override
    public InputStream retrieveRange(Long startPosition, Long endPosition) {
        try {
            var is = new BufferedInputStream(itemStore.readFile(filePath));
            if (startPosition != null && startPosition > 0) {
                is.skip(startPosition);
            }
            if (endPosition == null) {
                return is;
            }
            long start = startPosition == null ? 0L : startPosition;
            long length = endPosition - start + 1;
            return new BoundedInputStream(is, length);
        }
        catch (IOException e) {
            throw OcflIOException.from(e);
        }
    }
}
