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

import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.ocfl.api.model.User;
import io.ocfl.api.model.VersionInfo;
import io.ocfl.core.OcflRepositoryBuilder;
import io.ocfl.core.extension.OcflExtensionConfig;
import io.ocfl.core.storage.common.Storage;
import nl.knaw.dans.layerstore.ItemRecord;
import nl.knaw.dans.layerstore.ItemStore;
import nl.knaw.dans.layerstore.LayerDatabase;
import nl.knaw.dans.layerstore.LayerDatabaseImpl;
import nl.knaw.dans.layerstore.LayerManager;
import nl.knaw.dans.layerstore.LayeredItemStore;
import nl.knaw.dans.lib.util.PersistenceProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;

@ExtendWith(DropwizardExtensionsSupport.class)
public abstract class LayerDatabaseFixture extends AbstractTestWithTestDir {
    protected final DAOTestExtension daoTestExtension = DAOTestExtension.newBuilder()
        .addEntityClass(ItemRecord.class)
        .build();
    protected LayerDatabase dao;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        dao = new LayerDatabaseImpl(new PersistenceProviderImpl(daoTestExtension.getSessionFactory()));
    }

    protected LayeredStorage createLayeredStorage(LayerManager layerManager) {
        ItemStore itemStore = new LayeredItemStore(dao, layerManager, new StoreInventoryDbBackedContentManager());
        return new LayeredStorage(itemStore);
    }

    protected VersionInfo createVersionInfo(String message) {
        return new VersionInfo()
            .setMessage(message)
            .setUser(new User()
                .setName("test-user")
                .setAddress("mailto:somebody@dans.knaw.nl")
            );
    }

    protected OcflRepositoryBuilder createRepoBuilder(Storage storage, OcflExtensionConfig layoutConfig) throws IOException {
        return new OcflRepositoryBuilder()
            .defaultLayoutConfig(layoutConfig)
            .inventoryCache(null)
            .storage(ocflStorageBuilder -> ocflStorageBuilder.storage(storage))
            .workDir(Files.createDirectories(testDir.resolve("ocfl-work")));
    }
}
