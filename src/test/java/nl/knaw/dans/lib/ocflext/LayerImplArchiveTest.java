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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LayerImplArchiveTest extends AbstractTestWithTestDir {

    @Test
    public void archive_throws_IllegalStateException_when_layer_is_closed() throws Exception {
        var layer = new LayerImpl(1, testDir.resolve("staging"), new ZipArchive(testDir.resolve("test.zip")));
        assertThrows(IllegalStateException.class, layer::archive);
    }

    @Test
    public void archive_throws_IllegalStateException_when_layer_is_already_archived() throws Exception {
        var layer = new LayerImpl(1, testDir.resolve("staging"), new ZipArchive(testDir.resolve("test.zip")));
        layer.close();
        layer.archive();
        assertThrows(IllegalStateException.class, layer::archive);
    }

    @Test
    public void archive_removes_staging_dir() throws Exception {
        var layer = new LayerImpl(1, testDir.resolve("staging"), new ZipArchive(testDir.resolve("test.zip")));
        layer.close();
        layer.archive();
        assertThat(testDir.resolve("staging")).doesNotExist();
    }

}
