package nl.knaw.dans.lib.ocflext;

import io.ocfl.api.model.User;
import io.ocfl.api.model.VersionInfo;
import io.ocfl.core.OcflRepositoryBuilder;
import io.ocfl.core.extension.OcflExtensionConfig;
import io.ocfl.core.extension.storage.layout.config.NTupleOmitPrefixStorageLayoutConfig;
import io.ocfl.core.storage.common.Storage;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static io.ocfl.api.model.ObjectVersionId.head;
import static org.assertj.core.api.Assertions.assertThat;

public class RountTripTest extends LayerDatabaseFixture {
    private final Path inputBaseDir = Path.of("src/test/resources/input-roundtrip");

    @Test
    public void create_repo_with_object_versions_in_multiple_layers() throws Exception {
        var inputDir = inputBaseDir.resolve("multi-layer");
        var layerManager = new LayerManagerImpl(stagingDir, archiveDir);
        var storage = createLayeredStorage(layerManager);
        var repo = createRepoBuilder(storage, new NTupleOmitPrefixStorageLayoutConfig().setDelimiter(":").setTupleSize(3)).build();
        repo.putObject(head("urn:00000001"), inputDir.resolve("01first"), createVersionInfo("first"));
        layerManager.newTopLayer();
        repo.putObject(head("urn:00000001"), inputDir.resolve("02second"), createVersionInfo("second"));
        repo.putObject(head("urn:00000001"), inputDir.resolve("03third"), createVersionInfo("third"));
        layerManager.newTopLayer();
        repo.putObject(head("urn:00000001"), inputDir.resolve("04fourth"), createVersionInfo("fourth"));
        layerManager.newTopLayer();
        var outDir = Files.createDirectories(testDir.resolve("out"));
        // TODO: wait for background thread to finish archiving before extracting OR make executor configurable, so that we can process synchronously.
        // extractTarFilesInOrder(testDir, outDir);
        // assertThat(repoValid(outDir)).isTrue();
    }

    private void extractTarFilesInOrder(Path inputDir, Path outDir) throws IOException {
        try (var stream = Files.walk(inputDir)) {
            var tars = stream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".tar"))
                .sorted()
                .collect(Collectors.toList());
            extractTarFiles(tars, outDir);
        }
    }

    @SneakyThrows
    private boolean repoValid(Path storageRoot) {
        var process = Runtime.getRuntime().exec(String.format("rocfl -r %s validate", storageRoot));
        return process.waitFor() == 0;
    }

    @SneakyThrows
    private void extractTarFiles(List<Path> tars, Path outDir) {
        for (var tar : tars) {
            System.out.println("Extracting " + tar);
            try (var tarFile = new TarFile(tar)) {
                tarFile.getEntries().forEach(entry -> {
                    try {
                        var outPath = outDir.resolve(entry.getName());
                        if (entry.isDirectory()) {
                            Files.createDirectories(outPath);
                        }
                        else {
                            Files.createDirectories(outPath.getParent());
                            // Overwrite existing files
                            Files.deleteIfExists(outPath);
                            Files.copy(tarFile.getInputStream(entry), outPath);
                        }
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    private VersionInfo createVersionInfo(String message) {
        return new VersionInfo()
            .setMessage(message)
            .setUser(new User()
                .setName("test-user")
                .setAddress("mailto:somebody@dans.knaw.nl")
            );
    }

    private OcflRepositoryBuilder createRepoBuilder(Storage storage, OcflExtensionConfig layoutConfig) throws IOException {
        return new OcflRepositoryBuilder()
            .defaultLayoutConfig(layoutConfig)
            .inventoryCache(null)
            .storage(ocflStorageBuilder -> ocflStorageBuilder.storage(storage))
            .workDir(Files.createDirectories(testDir.resolve("ocfl-work")));
    }

}
