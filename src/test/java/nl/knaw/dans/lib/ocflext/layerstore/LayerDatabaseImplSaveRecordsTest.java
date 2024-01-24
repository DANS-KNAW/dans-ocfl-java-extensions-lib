/*
 * Copyright (C) 2023 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.lib.ocflext.layerstore;

import io.ocfl.core.storage.common.Listing;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class LayerDatabaseImplSaveRecordsTest extends LayerDatabaseFixture {

    @Test
    public void should_accept_empty_list() {
        dao.saveRecords();
        assertThat(dao.getAllRecords().toList()).asList().isEmpty();
    }

    @Test
    public void should_accept_one_record() {
        var record = ListingRecord2.builder()
            .layerId(1L)
            .path("path")
            .type(Listing.Type.Directory)
            .build();
        dao.saveRecords(record);
        assertThat(dao.getAllRecords())
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(record);
    }

    @Test
    public void should_accept_two_records() {
        var record1 = ListingRecord2.builder()
            .layerId(1L)
            .path("path1")
            .type(Listing.Type.Directory)
            .build();
        var record2 = ListingRecord2.builder()
            .layerId(2L)
            .path("path2")
            .type(Listing.Type.Directory)
            .build();
        dao.saveRecords(record1, record2);
        assertThat(dao.getAllRecords())
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("generatedId")
            .containsExactlyInAnyOrder(record1, record2);
    }
}
