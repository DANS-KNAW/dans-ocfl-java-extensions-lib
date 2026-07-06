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
package nl.knaw.dans.lib.ocflext;

import nl.knaw.dans.layerstore.Item;
import nl.knaw.dans.layerstore.ItemStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the contract-enforcement logic of {@link LayeredStorage} using a mock {@link ItemStore}.
 * These tests focus on verifying that the correct calls are made to the store and that the correct
 * exceptions are thrown, independent of the underlying ItemStore implementation's persistence behavior.
 */
public class LayeredStorageContractTest {

    private ItemStore itemStore;

    private LayeredStorage storage;

    @BeforeEach
    public void setUp() {
        itemStore = mock(ItemStore.class);
        storage = new LayeredStorage(itemStore);
    }

    // --- deleteEmptyDirsDown ---

    @Test
    public void deleteEmptyDirsDown_deletes_starting_path_when_it_is_empty() throws Exception {
        when(itemStore.listRecursive("a/b")).thenReturn(Collections.emptyList());
        when(itemStore.listDirectory("a/b")).thenReturn(Collections.emptyList());

        storage.deleteEmptyDirsDown("a/b");

        verify(itemStore).deleteDirectory("a/b");
    }

    @Test
    public void deleteEmptyDirsDown_does_not_delete_starting_path_when_it_has_contents() throws Exception {
        when(itemStore.listRecursive("a/b")).thenReturn(Collections.emptyList());
        when(itemStore.listDirectory("a/b")).thenReturn(List.of(Item.builder().path("a/b/file.txt").type(Item.Type.File).build()));

        storage.deleteEmptyDirsDown("a/b");

        verify(itemStore, never()).deleteDirectory("a/b");
    }

    @Test
    public void deleteEmptyDirsDown_deletes_empty_subdirectory_but_not_non_empty_starting_path() throws Exception {
        when(itemStore.listRecursive("a/b")).thenReturn(List.of(Item.builder().path("a/b/empty").type(Item.Type.Directory).build()));
        when(itemStore.listDirectory("a/b/empty")).thenReturn(Collections.emptyList());
        when(itemStore.listDirectory("a/b")).thenReturn(List.of(Item.builder().path("a/b/file.txt").type(Item.Type.File).build()));

        storage.deleteEmptyDirsDown("a/b");

        verify(itemStore).deleteDirectory("a/b/empty");
        verify(itemStore, never()).deleteDirectory("a/b");
    }

    // --- deleteEmptyDirsUp ---

    @Test
    public void deleteEmptyDirsUp_deletes_starting_path_and_walks_up_to_first_non_empty_ancestor() throws Exception {
        // a/b/c is empty; a/b becomes empty after c is deleted; a becomes empty after b is deleted
        when(itemStore.listDirectory("a/b/c")).thenReturn(Collections.emptyList());
        when(itemStore.listDirectory("a/b")).thenReturn(Collections.emptyList());
        when(itemStore.listDirectory("a")).thenReturn(Collections.emptyList());

        storage.deleteEmptyDirsUp("a/b/c");

        verify(itemStore).deleteDirectory("a/b/c");
        verify(itemStore).deleteDirectory("a/b");
        verify(itemStore).deleteDirectory("a");
    }

    @Test
    public void deleteEmptyDirsUp_stops_at_non_empty_ancestor() throws Exception {
        when(itemStore.listDirectory("a/b")).thenReturn(Collections.emptyList());
        // "a" has "a/c" — not empty
        when(itemStore.listDirectory("a")).thenReturn(List.of(Item.builder().path("a/c").type(Item.Type.Directory).build()));

        storage.deleteEmptyDirsUp("a/b");

        verify(itemStore).deleteDirectory("a/b");
        verify(itemStore, never()).deleteDirectory("a");
    }

    @Test
    public void deleteEmptyDirsUp_skips_non_existent_starting_path() throws Exception {
        when(itemStore.listDirectory("a/b")).thenThrow(new NoSuchFileException("a/b"));

        storage.deleteEmptyDirsUp("a/b");

        verify(itemStore, never()).deleteDirectory("a/b");
    }
}
