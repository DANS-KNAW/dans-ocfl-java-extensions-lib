package nl.knaw.dans.lib.ocflext;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@AllArgsConstructor
public class LayerImpl implements Layer {
    private final String id;
    private final LayerDatabase layerDatabase;
    private final Path stagingDir;
    private final Path archive;

    @Override
    public void createDirectories(String path) throws LayerNotWritableException, IOException {
        if (Files.exists(stagingDir)) {
            Files.createDirectories(stagingDir.resolve(path));
            layerDatabase.addDirectory(id, path);
        }
        else {
            throw new LayerNotWritableException();
        }
    }

    @Override
    public void deleteFiles(List<String> paths) {

    }

    @Override
    public void archive() {

    }

    @Override
    public InputStream read(String filePath) {
        return null;
    }
}
