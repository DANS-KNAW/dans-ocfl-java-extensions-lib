package nl.knaw.dans.lib.ocflext;

import io.ocfl.core.storage.common.Listing;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class LayerManagerImpl implements LayerManager {
    private final LayerDatabase layerDatabase;

    private final Path stagingRoot;

    private final Path archiveRoot;

    private LayerImpl topLayer;

    @Override
    public synchronized void openNewTopLayer() {
        if (topLayer != null) {
            topLayer.close();
        }
        var id = createId();
        topLayer = new LayerImpl(id, layerDatabase, stagingRoot.resolve(id), new ZipArchive(archiveRoot.resolve(id + ".zip")));
    }

    private static String createId() {
        // TODO: make sure that system time is always the same length
        return System.currentTimeMillis() + "-" + UUID.randomUUID();
    }

    @Override
    public synchronized void createDirectories(String path) throws LayerNotWritableException, IOException {
        topLayer.createDirectories(path);
    }

    @Override
    public synchronized void write(String filePath, InputStream content) throws LayerNotWritableException, IOException {
        topLayer.write(filePath, content);
    }

    @Override
    public InputStream read(String filePath) throws IOException {
        return null;
    }

    @Override
    public List<Layer> findLayersContaining(String path) {
        return null;
    }

    @Override
    public List<Listing> listDirectory(String directoryPath) {
        return null;
    }

    @Override
    public List<Listing> listRecursive(String directoryPath) {
        return null;
    }

    @Override
    public boolean fileExists(String filePath) {
        return false;
    }
}
