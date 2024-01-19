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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LayerDatabaseImplFindLayersContainingTest extends LayerDatabaseFixture {

    @Test
    public void findLayersContaining_should_return_empty_list_when_no_layers_contain_path() {
        daoTestExtension.inTransaction(() -> {
            saveDbRow(1L, "file1.txt", Listing.Type.File);
            saveDbRow(2L, "file2.txt", Listing.Type.File);
            saveDbRow(3L, "file3.txt", Listing.Type.File);
        });

        var result = daoTestExtension.inTransaction(() -> dao.findLayersContaining("file4.txt"));
        assertThat(result).asList().isEmpty();
    }

    @Test
    public void findLayersContaining_should_return_layerId_when_found() {
        daoTestExtension.inTransaction(() -> {
            saveDbRow(1L, "file1.txt", Listing.Type.File);
            saveDbRow(2L, "file2.txt", Listing.Type.File);
            saveDbRow(3L, "file3.txt", Listing.Type.File);
        });

        var result = daoTestExtension.inTransaction(() -> dao.findLayersContaining("file2.txt"));
        assertThat(result).asList().containsExactly(2L);
    }

    @Test
    public void findLayersContaining_should_return_multiple_layerIds_when_multiple_layers_contain_path() {
        daoTestExtension.inTransaction(() -> {
            saveDbRow(1L, "file1.txt", Listing.Type.File);
            saveDbRow(2L, "file2.txt", Listing.Type.File);
            saveDbRow(3L, "file3.txt", Listing.Type.File);
            saveDbRow(4L, "file2.txt", Listing.Type.File);
        });

        var result = daoTestExtension.inTransaction(() -> dao.findLayersContaining("file2.txt"));
        assertThat(result).asList().containsExactly(2L, 4L);
    }


}
