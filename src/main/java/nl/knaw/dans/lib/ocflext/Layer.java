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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * A layer is a collection of files and directories, that is intended to be stacked on top of other layers. Files in newer layers overwrite files in older layers with the same path. A layer can be
 * backed by a staging directory or an archive file.
 * </p>
 * <p>
 * A layer can be open, closing or closed:
 * <ul>
 * <li>"Open" means the layer is writable.</li>
 * <li>"Closing" means the layer is no longer writable, but the backing archive file has not yet fully been written. It can also mean than the archive file is in the process of being recreated because of a delete operation. </li>
 * <li>"Closed" means the layer is no longer writable and the backing file has been written.</li>
 * </p>
 */
public interface Layer {
    enum State {
        OPEN, CLOSING, CLOSED
    }

    /**
     * Deletes the files pointed to by <code>paths</code>. If the layer is closed, the backing archive file is first unarchived to a staging directory, the files are deleted, and the archive is
     * recreated. Not allowed if the layer is closing.
     *
     * @param paths the paths of the files to be deleted
     */
    void deleteFiles(List<String> paths);


    InputStream read(String path) throws IOException;

    /**
     * Archives the layer and then closes it. Pending writes must still be allowed to finish, before the layer may be archived.
     */
    void close();

    /**
     * Turns the layer into an archive file. This is only allowed if the layer is closed
     */
    void archive();

}
