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

import org.junit.jupiter.api.Test;

import static io.ocfl.api.OcflConstants.DEFAULT_OCFL_VERSION;
import static io.ocfl.api.OcflConstants.OBJECT_NAMASTE_PREFIX;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LayeredStorageOcflObjectRootDirIteratorTest extends LayerDatabaseFixture {

    @Test
    public void hasNext_should_return_false_directly_if_database_is_empty() {
        try (var iterator = new LayeredStorageOcflObjectRootDirIterator(dao)) {
            assertThat(iterator.hasNext()).isFalse();
        }
    }

    @Test
    public void hasNext_should_return_false_if_database_contains_records_but_none_for_ocfl_object_root_namaste() {
        daoTestExtension.inTransaction(() -> {
                dao.addDirectories(1L, "root");
                dao.addFile(1L, "root/file.txt");
                dao.addFile(1L, "root/file2.txt");
            }
        );
        try (var iterator = new LayeredStorageOcflObjectRootDirIterator(dao)) {
            assertThat(iterator.hasNext()).isFalse();
        }
    }

    @Test
    public void next_should_return_object_if_located_directly_under_storage_root() {
        daoTestExtension.inTransaction(() -> {
                dao.addDirectories(1L, "root");
                dao.addFile(1L, "root/file.txt");
                dao.addFile(1L, "root/file2.txt");
                dao.addFile(1L, "root/" + OBJECT_NAMASTE_PREFIX + DEFAULT_OCFL_VERSION);
            }
        );
        try (var iterator = new LayeredStorageOcflObjectRootDirIterator(dao)) {
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("root");
        }
    }

    @Test
    public void hasNext_should_return_true_if_ocfl_objects_are_several_subdirectories_under_ocfl_storage_root() {
        daoTestExtension.inTransaction(() -> {
            dao.addDirectories(1L, "root/subdir1/subdir2");
            dao.addFile(1L, "root/subdir1/subdir2/file.txt");
            dao.addFile(1L, "root/subdir1/subdir2/file2.txt");
            dao.addFile(1L, "root/subdir1/subdir2/" + OBJECT_NAMASTE_PREFIX + DEFAULT_OCFL_VERSION);
        });

        try (var iterator = new LayeredStorageOcflObjectRootDirIterator(dao)) {
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("root/subdir1/subdir2");
        }
    }

    @Test
    public void next_should_return_next_ocfl_object_root_directory() {
        daoTestExtension.inTransaction(() -> {
            dao.addDirectories(1L, "root/subdir1/subdir2");
            dao.addFile(1L, "root/subdir1/subdir2/file.txt");
            dao.addFile(1L, "root/subdir1/subdir2/file2.txt");
            dao.addFile(1L, "root/subdir1/subdir2/" + OBJECT_NAMASTE_PREFIX + DEFAULT_OCFL_VERSION);
            dao.addDirectories(1L, "root/subdir3/subdir4");
            dao.addFile(1L, "root/subdir3/subdir4/file.txt");
            dao.addFile(1L, "root/subdir3/subdir4/file2.txt");
            dao.addFile(1L, "root/subdir3/subdir4/" + OBJECT_NAMASTE_PREFIX + DEFAULT_OCFL_VERSION);
        });

        try (var iterator = new LayeredStorageOcflObjectRootDirIterator(dao)) {
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("root/subdir1/subdir2");
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("root/subdir3/subdir4");
            assertThat(iterator.hasNext()).isFalse();
        }
    }

    @Test
    public void next_should_return_next_ocfl_object_root_directory_in_next_layer() {
        daoTestExtension.inTransaction(() -> {
            dao.addDirectories(1L, "root/subdir1/subdir2");
            dao.addFile(1L, "root/subdir1/subdir2/file.txt");
            dao.addFile(1L, "root/subdir1/subdir2/file2.txt");
            dao.addFile(1L, "root/subdir1/subdir2/" + OBJECT_NAMASTE_PREFIX + DEFAULT_OCFL_VERSION);
            dao.addDirectories(2L, "root/subdir3/subdir4");
            dao.addFile(2L, "root/subdir3/subdir4/file.txt");
            dao.addFile(2L, "root/subdir3/subdir4/file2.txt");
            dao.addFile(2L, "root/subdir3/subdir4/" + OBJECT_NAMASTE_PREFIX + DEFAULT_OCFL_VERSION);
        });

        try (var iterator = new LayeredStorageOcflObjectRootDirIterator(dao)) {
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("root/subdir1/subdir2");
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("root/subdir3/subdir4");
            assertThat(iterator.hasNext()).isFalse();
        }
    }

    @Test
    // mixed depth of subdirectories
    public void next_should_return_next_ocfl_object_root_directory_in_next_layer_and_different_depth() {
        daoTestExtension.inTransaction(() -> {
            dao.addDirectories(1L, "root/subdir1/subdir2");
            dao.addFile(1L, "root/subdir1/subdir2/file.txt");
            dao.addFile(1L, "root/subdir1/subdir2/file2.txt");
            dao.addFile(1L, "root/subdir1/subdir2/" + OBJECT_NAMASTE_PREFIX + DEFAULT_OCFL_VERSION);
            dao.addDirectories(2L, "root/subdir3");
            dao.addFile(2L, "root/subdir3/file.txt");
            dao.addFile(2L, "root/subdir3/file2.txt");
            dao.addFile(2L, "root/subdir3/" + OBJECT_NAMASTE_PREFIX + DEFAULT_OCFL_VERSION);
        });

        try (var iterator = new LayeredStorageOcflObjectRootDirIterator(dao)) {
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("root/subdir1/subdir2");
            assertThat(iterator.hasNext()).isTrue();
            assertThat(iterator.next()).isEqualTo("root/subdir3");
            assertThat(iterator.hasNext()).isFalse();
        }
    }
}
