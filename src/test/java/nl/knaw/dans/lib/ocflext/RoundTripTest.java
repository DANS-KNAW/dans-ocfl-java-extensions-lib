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

import io.dropwizard.util.DirectExecutorService;
import io.ocfl.api.OcflRepository;
import io.ocfl.core.extension.storage.layout.config.NTupleOmitPrefixStorageLayoutConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.knaw.dans.layerstore.DirectLayerArchiver;
import nl.knaw.dans.layerstore.LayerManager;
import nl.knaw.dans.layerstore.LayerManagerImpl;
import nl.knaw.dans.layerstore.ZipArchiveProvider;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static io.ocfl.api.model.ObjectVersionId.head;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class RoundTripTest extends LayerDatabaseFixture {
    private final Path inputBaseDir = Path.of("src/test/resources/input");

    private OcflRepository repo;
    private LayerManager layerManager;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        layerManager = new LayerManagerImpl(stagingDir, new ZipArchiveProvider(archiveDir), new DirectLayerArchiver());
        repo = createRepoBuilder(createLayeredStorage(layerManager), new NTupleOmitPrefixStorageLayoutConfig().setDelimiter(":").setTupleSize(3)).build();
    }

    private boolean rocflOnPath() {
        return Arrays.stream(System.getenv("PATH").split(":"))
            .anyMatch(path -> Files.exists(Paths.get(path, "rocfl")));
    }

    private void putObject(String series, String input, String id) {
        repo.putObject(head("urn:" + id), inputBaseDir.resolve(series).resolve(input), createVersionInfo(id));
    }

    @Test
    public void create_repo_with_object_versions_in_multiple_layers() throws Exception {
        var series = "series001";
        Assumptions.assumeTrue(rocflOnPath());
        putObject(series, "01", "001");
        layerManager.newTopLayer();
        putObject(series, "02", "001");
        putObject(series, "03", "001");
        layerManager.newTopLayer();
        putObject(series, "04", "001");
        assertThat(repo.listObjectIds().toList()).containsExactly("urn:001");
        layerManager.newTopLayer(); // Trigger archiving of the top layer
        var outDir = Files.createDirectories(testDir.resolve("out"));
        TestUtil.extractZipFilesInOrder(archiveDir, outDir);
        assertThat(repoValid(outDir)).isTrue();
    }

    @Test
    public void create_version_series_with_larger_files_additions_renames_and_deletions() throws Exception {
        var series = "series002";
        Assumptions.assumeTrue(rocflOnPath());
        putObject(series, "01", "002");
        layerManager.newTopLayer();
        putObject(series, "02", "002");
        putObject(series, "03", "002");
        layerManager.newTopLayer();
        var outDir = Files.createDirectories(testDir.resolve("out"));
        TestUtil.extractZipFilesInOrder(archiveDir, outDir);
        assertThat(repoValid(outDir)).isTrue();
    }

    @SneakyThrows
    private boolean repoValid(Path storageRoot) {
        var process = Runtime.getRuntime().exec(String.format("rocfl -r %s validate", storageRoot));
        return process.waitFor() == 0;
    }

}
