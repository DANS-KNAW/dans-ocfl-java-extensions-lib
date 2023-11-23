package nl.knaw.dans.lib.ocflext;

import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;

class LayerImpl implements Layer {
    private final String id;
    private final LayerDatabase layerDatabase;
    private final Path stagingDir;
    private final Archive archive;
    private final MultiLock writeLocks = new MultiLock();

    private final MultiLock readLocks = new MultiLock();

    private boolean isClosed = false;

    private boolean isArchived = false;

    private ExecutorService executorService;

    LayerImpl(String id, LayerDatabase layerDatabase, Path stagingDir, Archive archive) {
        this.id = id;
        this.layerDatabase = layerDatabase;
        this.stagingDir = stagingDir;
        this.archive = archive;
    }

    @SneakyThrows
    void createDirectories(String path) {
        checkOpen();
        writeLocks.incrementLock();
        try {
            Files.createDirectories(stagingDir.resolve(path));
            layerDatabase.addDirectory(id, path);
        }
        finally {
            writeLocks.decrementLock();
        }
    }

    @SneakyThrows
    void write(String filePath, InputStream content) {
        checkOpen();
        writeLocks.incrementLock();
        try {
            Files.copy(content, stagingDir.resolve(filePath));
            layerDatabase.addFile(id, filePath);
        }
        finally {
            writeLocks.decrementLock();
        }
    }

    private synchronized void checkOpen() {
        if (isClosed) {
            throw new IllegalStateException("Layer is closed");
        }
    }

    private synchronized void checkClosed() {
        if (!isClosed) {
            throw new IllegalStateException("Layer is not closed");
        }
    }

    private synchronized void checkUnarchived() {
        if (isArchived) {
            throw new IllegalStateException("Layer is already archived");
        }
    }

    @Override
    public void deleteFiles(List<String> paths) {
    }

    @Override
    public synchronized void close() {
        isClosed = true;
    }

    @SneakyThrows
    public synchronized void archive() {
        checkClosed();
        checkUnarchived();
        try {
            writeLocks.acquire();
            archive.archiveFrom(stagingDir);
            isArchived = true;
            executorService.submit(() -> {
                /* All new reads should be done from the archive. However, reads may still be in progress. We need to wait for them to finish,
                 * before we can delete the staging directory. This can be done asynchronously, because the archive is immutable.
                 */
                try {
                    readLocks.acquire();
                    Files.delete(stagingDir);
                }
                finally {
                    readLocks.release();
                }
            });
        }
        finally {
            writeLocks.release();
        }
    }

    @Override
    public InputStream read(String filePath) throws IOException {
        if (isArchived) {
            return archive.read(filePath);
        }
        else {
            return new CloseNotifyingInputStream(Files.newInputStream(stagingDir.resolve(filePath)), readLocks::decrementLock);
        }
    }
}
