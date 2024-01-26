package nl.knaw.dans.lib.ocflext.layerstore;

import org.junit.jupiter.api.Test;

import static nl.knaw.dans.lib.ocflext.layerstore.Item.Type;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LayerDatabaseDeleteByIdsTest extends LayerDatabaseFixture {

    @Test
    public void should_accept_empty_list() {
        addToDb(1L, "path", Type.Directory);
        daoTestExtension.inTransaction(() -> dao.deleteRecordsById());
        // Check that the record is still there
        assertThat(dao.getAllRecords().toList()).asList().hasSize(1);
    }

    @Test
    public void should_delete_one_record() {
        var record = addToDb(1L, "path", Type.Directory);
        daoTestExtension.inTransaction(() -> dao.deleteRecordsById(record.getGeneratedId()));
        assertThat(dao.getAllRecords().toList()).asList().isEmpty();
    }

    @Test
    public void should_delete_two_records() {
        var record1 = addToDb(1L, "path1", Type.Directory);
        var record2 = addToDb(2L, "path2", Type.File);
        var notDeletedRecord = addToDb(3L, "path3", Type.Directory);
        daoTestExtension.inTransaction(() -> dao.deleteRecordsById(record1.getGeneratedId(), record2.getGeneratedId()));
        assertThat(dao.getAllRecords().toList()).asList()
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(notDeletedRecord);
    }

}
