package nl.knaw.dans.lib.ocflext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * An archive is a file that contains a collection of files and directories. It can be implemented as a zip file, a tar, etc.
 */
public interface Archive {

    /**
     * Returns an input stream for the file at the given path.
     *
     * @param filePath the path of the file to read
     * @return the input stream
     * @throws IOException if an I/O error occurs
     */
    InputStream read(String filePath) throws IOException;

    /**
     * Unarchives the archive to the given staging directory.
     *
     * @param stagingDir the directory to unarchive to
     */
    void unarchiveTo(Path stagingDir);

    /**
     * Archives the given staging directory overwriting the backing file, if it exists.
     *
     * @param stagingDir the directory to archive
     */
    void archiveFrom(Path stagingDir);
}
