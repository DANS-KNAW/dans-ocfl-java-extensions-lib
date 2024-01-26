package nl.knaw.dans.lib.ocflext.layerstore;

import lombok.Data;

/**
 * A file or directory in the layer store. It may be represented by multiple ItemRecords in the database, one for each layer that contains the item. Note, that to obtain the current (i.e. latest)
 * content of a File item, you need to retrieve the ItemRecord with the highest layerId.
 */
@Data
public class Item {
    public enum Type {
        File,
        Directory
    }

    private final String path;
    private final Type type;
}
