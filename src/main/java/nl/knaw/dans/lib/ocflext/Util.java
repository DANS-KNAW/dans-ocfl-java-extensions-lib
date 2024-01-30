package nl.knaw.dans.lib.ocflext;

import io.ocfl.core.storage.common.Listing;
import nl.knaw.dans.layerstore.Item;

import java.nio.file.Path;

public class Util {

    /**
     * Convert an Item to a Listing.
     *
     * @param item           the item to convert
     * @param relativeToPath the path to relativize the item path to
     * @return the converted item
     */
    public static Listing fromItem(Item item, String relativeToPath) {
        var itemPath = Path.of(item.getPath());
        var relativeTo = Path.of(relativeToPath);
        var relativePath = relativeTo.relativize(itemPath);

        return switch (item.getType()) {
            case File -> Listing.file(relativePath.toString());
            case Directory -> Listing.directory(relativePath.toString());
        };
    }
}
