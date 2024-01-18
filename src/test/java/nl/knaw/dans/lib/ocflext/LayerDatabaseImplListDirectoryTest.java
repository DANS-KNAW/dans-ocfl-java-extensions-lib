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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class LayerDatabaseImplListDirectoryTest extends LayerDatabaseFixture {

    @Test
    public void listDirectory_should_return_empty_list_if_nothing_found() {
        assertThat(dao.listDirectory("")).asList().isEmpty();
    }

    @Test
    public void listDirectory_should_return_list_of_items_in_root_folder_if_parameter_is_empty_string() {
        // Add some records to find
        daoTestExtension.inTransaction(() -> {
            saveDbRow(1L, "subdir", Listing.Type.Directory);
            saveDbRow(1L, "file1", Listing.Type.File);
            saveDbRow(1L, "file2", Listing.Type.File);
            saveDbRow(2L, "subdir/file3", Listing.Type.Directory);
        });

        assertThat(dao.listDirectory("")).asList()
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(
                ListingRecord.builder()
                    .layerId(1L)
                    .path("subdir")
                    .type(Listing.Type.Directory)
                    .build(),
                ListingRecord.builder()
                    .layerId(1L)
                    .path("file1")
                    .type(Listing.Type.File)
                    .build(),
                ListingRecord.builder()
                    .layerId(1L)
                    .path("file2")
                    .type(Listing.Type.File)
                    .build()
            );
    }

    @Test
    public void listDirectory_should_return_list_of_items_in_subdir_folder_if_parameter_path_to_that_folder() {
        // Add some records to find
        daoTestExtension.inTransaction(() -> {
            saveDbRow(1L, "subdir", Listing.Type.Directory);
            saveDbRow(1L, "subdir/file1", Listing.Type.File);
            saveDbRow(1L, "subdir/file2", Listing.Type.File);
            saveDbRow(2L, "subdir/subsubdir", Listing.Type.Directory);
            saveDbRow(2L, "otherdir", Listing.Type.Directory);
        });

        assertThat(dao.listDirectory("subdir")).asList()
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(
                ListingRecord.builder()
                    .layerId(1L)
                    .path("subdir/file1")
                    .type(Listing.Type.File)
                    .build(),
                ListingRecord.builder()
                    .layerId(1L)
                    .path("subdir/file2")
                    .type(Listing.Type.File)
                    .build(),
                ListingRecord.builder()
                    .layerId(2L)
                    .path("subdir/subsubdir")
                    .type(Listing.Type.Directory)
                    .build()
            );
    }

    @Test
    public void listDirectory_should_return_latest_version_of_file_if_it_exists_in_multiple_layers() {
        // Add some records to find
        daoTestExtension.inTransaction(() -> {
            saveDbRow(1L, "file1", Listing.Type.File);
            saveDbRow(2L, "file1", Listing.Type.File);
            saveDbRow(3L, "file1", Listing.Type.File);
        });

        assertThat(dao.listDirectory("")).asList()
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(
                ListingRecord.builder()
                    .layerId(3L)
                    .path("file1")
                    .type(Listing.Type.File)
                    .build()
            );
    }



    @Test
    public void listDirectory_should_throw_an_IllegalArgumentException_if_parameter_is_null() {
        assertThatThrownBy(() -> dao.listDirectory(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("directoryPath must not be null");
    }

}
