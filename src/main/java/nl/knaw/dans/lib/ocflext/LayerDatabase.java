package nl.knaw.dans.lib.ocflext;

import io.ocfl.core.storage.common.Listing;

import java.util.List;
import java.util.Set;

public interface LayerDatabase {

    Set<Listing> listDirectory(String directoryPath);

    Set<Listing> listRecursive(String directoryPath);

    void addDirectory(String id, String path);

    void addFile(String id, String filePath);

    void delete(String id, String path);
}
