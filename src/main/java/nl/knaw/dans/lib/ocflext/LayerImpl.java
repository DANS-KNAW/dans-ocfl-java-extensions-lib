package nl.knaw.dans.lib.ocflext;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class LayerImpl implements Layer {
    private final String id;
    private final LayerDatabase layerDatabase;
    private final Path stagingDir;
    private final Archive archive;
    private State state;

    LayerImpl(String id, LayerDatabase layerDatabase, Path stagingDir, Archive archive) {
        this.id = id;
        this.layerDatabase = layerDatabase;
        this.stagingDir = stagingDir;
        this.archive = archive;
        this.state = State.OPEN;
    }

    void createDirectories(String path) {
        checkStateForWriting();

    }

    void write(String filePath, InputStream content) {
        checkStateForWriting();

    }

    private void checkStateForWriting() {
        if (state == State.CLOSED || state == State.CLOSING) {
            throw new IllegalStateException(String.format("Layer in invalid state for writing %s", state));
        }
    }

    @Override
    public void deleteFiles(List<String> paths) {
        if (state == State.CLOSED) {
            archive.unarchiveTo(stagingDir);
            for (String path : paths) {
                try {
                    Files.delete(stagingDir.resolve(path));
                    layerDatabase.delete(id, path);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            archive.archiveFrom(stagingDir);
        }
        else if (state == State.OPEN) {
            for (String path : paths) {
                try {
                    Files.delete(stagingDir.resolve(path));
                    layerDatabase.delete(id, path);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else {
            throw new IllegalStateException(String.format("Layer in invalid state for deleting %s", state));
        }
    }

    @Override
    public void close() {
        state = State.CLOSING;
        archive.archiveFrom(stagingDir);
        FileUtils.deleteQuietly(stagingDir.toFile());
        state = State.CLOSED;
    }

    InputStream read(String filePath) throws IOException {
        if (state == State.CLOSED) {
            return archive.read(filePath);
        }
        else {
            return Files.newInputStream(stagingDir.resolve(filePath));
        }
    }
}
