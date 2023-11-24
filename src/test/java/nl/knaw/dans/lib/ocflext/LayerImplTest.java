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

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class LayerImplTest extends AbstractTestWithTestDir {
    private LayerDatabase layerDatabase = Mockito.mock(LayerDatabase.class);

    @Test
    public void constructor_should_create_working_directories() {
        Archive zipArchive = new ZipArchive(testDir.resolve("test.zip"));
        new LayerImpl(1L, layerDatabase, testDir.resolve("test-layer"), zipArchive);

        assertThat(testDir.resolve("test-layer")).isDirectory();
        assertThat(testDir.resolve("test-layer/ocfl-staging")).isDirectory();
        assertThat(testDir.resolve("test-layer/mutation-tokens")).isDirectory();
    }

    @Test
    public void createDirectories_should_create_directories_in_staging_dir() throws Exception {
        Archive zipArchive = new ZipArchive(testDir.resolve("test.zip"));
        LayerImpl layer = new LayerImpl(1L, layerDatabase, testDir.resolve("test-layer"), zipArchive);

        layer.createDirectories("test-dir/subdir");

        assertThat(testDir.resolve("test-layer/ocfl-staging/test-dir/subdir")).exists();
    }

    @Test
    public void write_should_write_file_to_staging_dir() throws Exception {
        Archive zipArchive = new ZipArchive(testDir.resolve("test.zip"));
        var expectedContent = "Test content";
        FileUtils.writeStringToFile(testDir.resolve("test-input/test-file.txt").toFile(), expectedContent, "UTF-8");
        LayerImpl layer = new LayerImpl(1L, layerDatabase, testDir.resolve("test-layer"), zipArchive);

        layer.write("test-file.txt", FileUtils.openInputStream(testDir.resolve("test-input/test-file.txt").toFile()));

        assertThat(testDir.resolve("test-layer/ocfl-staging/test-file.txt")).exists();
        assertThat(testDir.resolve("test-layer/ocfl-staging/test-file.txt")).hasContent(expectedContent);
    }

    @Test
    public void write_should_fail_if_layer_is_closed() throws Exception {
        Archive zipArchive = new ZipArchive(testDir.resolve("test.zip"));
        var expectedContent = "Test content";
        FileUtils.writeStringToFile(testDir.resolve("test-input/test-file.txt").toFile(), expectedContent, "UTF-8");
        LayerImpl layer = new LayerImpl(1L, layerDatabase, testDir.resolve("test-layer"), zipArchive);
        layer.close();

        assertThatThrownBy(() -> layer.write("test-file.txt", FileUtils.openInputStream(testDir.resolve("test-input/test-file.txt").toFile())))
            .isInstanceOf(LayerNotWritableException.class)
            .hasMessage("Layer is closed");
    }

    @Test
    public void archive_should_fail_if_layer_is_still_open() throws Exception {
        Archive zipArchive = new ZipArchive(testDir.resolve("test.zip"));
        var expectedContent = "Test content";
        FileUtils.writeStringToFile(testDir.resolve("test-input/test-file.txt").toFile(), expectedContent, "UTF-8");
        LayerImpl layer = new LayerImpl(1L, layerDatabase, testDir.resolve("test-layer"), zipArchive);

        assertThatThrownBy(layer::archive)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Layer is not closed");
    }

    @Test
    public void archive_should_fail_is_layer_is_still_mutating() throws Exception {

    }

}
