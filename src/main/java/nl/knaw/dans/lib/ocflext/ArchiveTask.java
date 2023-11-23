package nl.knaw.dans.lib.ocflext;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Path;

@AllArgsConstructor
public class ArchiveTask implements Runnable {
    private final Archive archive;
    private final Path stagingDir;
    @Override
    public void run() {
        archive.archiveFrom(stagingDir);



    }
}
