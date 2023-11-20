package nl.knaw.dans.lib.ocflext;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@AllArgsConstructor
public class ZipArchive implements Archive{
    private final Path zipFile;

    @Override
    public InputStream read(String filePath) throws IOException {
        return null;
    }

    @Override
    public void unarchiveTo(Path stagingDir) {

    }

    @Override
    public void archiveFrom(Path stagingDir) {

    }
}
