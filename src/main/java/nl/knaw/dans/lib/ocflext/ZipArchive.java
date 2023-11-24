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

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
public class ZipArchive implements Archive {
    @NonNull
    private final Path zipFile;

    @Getter
    private boolean archived = false;

    @Override
    public InputStream read(String filePath) throws IOException {
        return null;
    }

    @Override
    public void unarchiveTo(Path stagingDir) {

    }

    @Override
    public void archiveFrom(Path stagingDir) {
        createZipFile(zipFile.toString(), stagingDir.toString());
    }

    // See: https://simplesolution.dev/java-create-zip-file-using-apache-commons-compress/
    @SneakyThrows
    public void createZipFile(String zipFileName, String directoryToZip) {
        BufferedOutputStream bufferedOutputStream = null;
        ZipArchiveOutputStream zipArchiveOutputStream = null;
        OutputStream outputStream = null;
        try {
            Path zipFilePath = Paths.get(zipFileName);
            outputStream = Files.newOutputStream(zipFilePath);
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            zipArchiveOutputStream = new ZipArchiveOutputStream(bufferedOutputStream);
            File[] files = new File(directoryToZip).listFiles();
            if (files != null) {
                for (File file : files) {
                    addFileToZipStream(zipArchiveOutputStream, file, "");
                }
            }
        }
        finally {
            IOUtils.closeQuietly(zipArchiveOutputStream);
            IOUtils.closeQuietly(bufferedOutputStream);
            IOUtils.closeQuietly(outputStream);
            archived = true;
        }
    }

    private void addFileToZipStream(ZipArchiveOutputStream zipArchiveOutputStream, File fileToZip, String base) throws IOException {
        String entryName = base + fileToZip.getName();
        ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(fileToZip, entryName);
        zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
        if (fileToZip.isFile()) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(fileToZip);
                IOUtils.copy(fileInputStream, zipArchiveOutputStream);
                zipArchiveOutputStream.closeArchiveEntry();
            }
            finally {
                IOUtils.closeQuietly(fileInputStream);
            }
        }
        else {
            zipArchiveOutputStream.closeArchiveEntry();
            File[] files = fileToZip.listFiles();
            if (files != null) {
                for (File file : files) {
                    addFileToZipStream(zipArchiveOutputStream, file, entryName + "/");
                }
            }
        }
    }
}
