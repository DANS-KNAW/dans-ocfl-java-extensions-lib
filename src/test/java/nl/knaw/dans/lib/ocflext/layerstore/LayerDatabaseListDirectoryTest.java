package nl.knaw.dans.lib.ocflext.layerstore;

import org.junit.jupiter.api.Test;

import static nl.knaw.dans.lib.ocflext.layerstore.Item.Type;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class LayerDatabaseListDirectoryTest extends LayerDatabaseFixture {

    @Test
    public void should_return_empty_list_when_no_records_in_db() throws Exception {
        assertThat(dao.listDirectory("")).asList().isEmpty();
    }

    @Test
    public void should_return_empty_list_when_no_items_in_directory() throws Exception {
        addToDb(1L, "dir", Type.Directory);
        assertThat(dao.listDirectory("dir")).asList().isEmpty();
    }

    @Test
    public void should_return_one_item_when_one_item_in_root_directory() throws Exception {
        var record = addToDb(1L, "item", Type.Directory);
        assertThat(dao.listDirectory("")).asList()
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(record);
    }

    @Test
    public void should_return_one_item_when_one_item_in_directory() throws Exception {
        addToDb(1L, "dir", Type.Directory);
        var record = addToDb(1L, "dir/file", Type.File);
        assertThat(dao.listDirectory("dir")).asList()
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(record);
    }

    @Test
    public void should_return_two_items_when_two_items_in_directory() throws Exception {
        addToDb(1L, "dir", Type.Directory);
        var record1 = addToDb(1L, "dir/file1", Type.File);
        var record2 = addToDb(1L, "dir/file2", Type.File);
        assertThat(dao.listDirectory("dir")).asList()
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(record1, record2);
    }

    @Test
    public void should_return_two_items_when_two_items_in_root_directory() throws Exception {
        var record1 = addToDb(1L, "file1", Type.File);
        var record2 = addToDb(1L, "file2", Type.File);
        assertThat(dao.listDirectory("")).asList()
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(record1, record2);
    }

    @Test
    public void should_return_one_item_when_item_has_records_in_multiple_layers() throws Exception {
        addToDb(1L, "dir", Type.Directory);
        addToDb(2L, "dir", Type.Directory);
        var record = addToDb(1L, "dir/file", Type.File);
        addToDb(2L, "dir/file", Type.File);
        assertThat(dao.listDirectory("dir")).asList()
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(record);
    }

    @Test
    public void should_return_one_item_when_item_has_grandchildren() throws Exception {
        addToDb(1L, "dir", Type.Directory);
        var record = addToDb(1L, "dir/subdir", Type.Directory);
        addToDb(1L, "dir/subdir/subfile", Type.File);
        assertThat(dao.listDirectory("dir")).asList()
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(record);
    }

    @Test
    public void should_throw_IllegalArgumentException_when_path_is_not_a_directory() throws Exception {
        addToDb(1L, "dir", Type.Directory);
        addToDb(1L, "dir/file", Type.File);
        assertThatThrownBy(() -> dao.listDirectory("dir/file"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Not a directory: dir/file");
    }

    @Test
    public void should_throw_IllegalArgumentException_when_path_does_not_exist() throws Exception {
        addToDb(1L, "dir", Type.Directory);
        addToDb(1L, "dir/file", Type.File);
        assertThatThrownBy(() -> dao.listDirectory("dir/does-not-exist"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No such directory: dir/does-not-exist");
    }

}
