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

import io.dropwizard.hibernate.AbstractDAO;
import io.ocfl.core.storage.common.Listing;
import org.hibernate.SessionFactory;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.util.List;

public class LayerDatabaseImpl extends AbstractDAO<ListingRecord> implements LayerDatabase {

    public LayerDatabaseImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public ListingRecord save(ListingRecord listingRecord) {
        if (listingRecord.getGeneratedId() == null) {
            return persist(listingRecord);
        }
        var currentListingRecord = get(listingRecord.getGeneratedId());
        if (currentListingRecord == null) {
            return persist(listingRecord);
        }
        else {
            try (var s = currentSession()) {
                return (ListingRecord) s.merge(listingRecord);
            }
        }
    }

    @Override
    public List<ListingRecord> listDirectory(String directoryPath) throws IOException {
        directoryPath = preprocessDirectoryArgument(directoryPath);
        return namedTypedQuery("ListingRecord.listDirectory")
            .setParameter("path", directoryPath + "%")
            .setParameter("pathWithTwoComponents", directoryPath + "%/%")
            .getResultList();
    }

    @Override
    public List<ListingRecord> listRecursive(String directoryPath) throws IOException {
        return namedTypedQuery("ListingRecord.listRecursive")
            .setParameter("path", preprocessDirectoryArgument(directoryPath) + "%")
            .getResultList();
    }

    private String preprocessDirectoryArgument(String directoryPath) throws IOException {
        if (directoryPath == null) {
            throw new IllegalArgumentException("directoryPath must not be null");
        }

        if (!directoryPath.isBlank()) {
            var records = getByPath(directoryPath);
            if (records.isEmpty()) {
                throw new NoSuchFileException(directoryPath);
            }
            if (records.stream().anyMatch(r -> r.getType() != Listing.Type.Directory)) {
                throw new NotDirectoryException(directoryPath);
            }
            // Add an ending slash to directoryPath, if it doesn't have one yet.
            if (!directoryPath.endsWith("/")) {
                directoryPath += "/";
            }
        }
        return directoryPath;
    }

    @Override
    public void addDirectories(long layerId, String path) {
        String[] pathComponents = path.split("/");
        String currentPath = "";

        for (String component : pathComponents) {
            currentPath = currentPath.isEmpty() ? component : currentPath + "/" + component;
            List<ListingRecord> records = getByPath(currentPath);

            if (!records.isEmpty()) {
                // If this path is occupied by a file, in any layer, then we have a problem.
                if (records.stream().anyMatch(r -> r.getType() != Listing.Type.Directory)) {
                    throw new IllegalArgumentException("Cannot add directory " + currentPath + " because it is already occupied by a file.");
                }
            }

            var recordsInLayer = records.stream().filter(r -> r.getLayerId() == layerId).toList();
            if (recordsInLayer.isEmpty()) {
                ListingRecord newRecord = new ListingRecord.Builder()
                    .layerId(layerId)
                    .path(currentPath)
                    .type(Listing.Type.Directory)
                    .build();
                save(newRecord);
            }
        }
    }

    private List<ListingRecord> getByPath(String path) {
        return namedTypedQuery("ListingRecord.getByPath")
            .setParameter("path", path)
            .getResultList();
    }

    @Override
    public void saveRecords(List<ListingRecord> records) {
        for (ListingRecord record : records) {
            save(record);
        }
    }

    @Override
    public void addFile(long layerId, String filePath) {
        saveRecords(List.of(
            new ListingRecord.Builder()
                .layerId(layerId)
                .path(filePath)
                .type(Listing.Type.File)
                .build()
        ));
    }

    @Override
    public void deleteRecords(List<ListingRecord> records) {
        for (ListingRecord record : records) {
            currentSession().delete(record);
        }
    }

    @Override
    public List<Long> findLayersContaining(String path) {
        return namedQuery("ListingRecord.findLayersContaining")
            .setParameter("path", path)
            .getResultList().stream().map(o -> (Long) o).toList();
    }

    @Override
    public byte[] readContentFromDatabase(String filePath) {
        var records = getByPath(filePath);
        if (records.isEmpty()) {
            throw new IllegalArgumentException("File '" + filePath + "' does not exist.");
        }
        if (records.stream().anyMatch(r -> r.getType() != Listing.Type.File)) {
            throw new IllegalArgumentException("'" + filePath + "' is not a file.");
        }
        var record = records.stream().max((r1, r2) -> (int) (r1.getLayerId() - r2.getLayerId())).get();
        return record.getContent();
    }

    @Override
    public boolean isContentStoredInDatabase(String filePath) {
        return namedQuery("ListingRecord.isContentStoredInDatabase")
            .setParameter("path", filePath)
            .getResultList().stream().anyMatch(o -> (Boolean) o);
    }

    public List<ListingRecord> listAll() {
        return super.list(namedTypedQuery("ListingRecord.listAll"));
    }
}
