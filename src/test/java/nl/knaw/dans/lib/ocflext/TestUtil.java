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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TestUtil {

    static void extractTarFilesInOrder(Path inputDir, Path outDir) throws IOException {
        try (var stream = Files.walk(inputDir)) {
            var tars = stream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".tar"))
                .sorted()
                .collect(Collectors.toList());
            extractTarFiles(tars, outDir);
        }
    }

    static void extractZipFilesInOrder(Path inputDir, Path outDir) throws IOException {
        try (var stream = Files.walk(inputDir)) {
            var zips = stream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".zip"))
                .sorted()
                .collect(Collectors.toList());
            extractZipFiles(zips, outDir);
        }
    }

    @SneakyThrows
    static void extractTarFiles(List<Path> tars, Path outDir) {
        for (var tar : tars) {
            log.debug("Extracting {}", tar);
            try (var tarFile = new TarFile(tar)) {
                tarFile.getEntries().forEach(entry -> {
                    try {
                        var outPath = outDir.resolve(entry.getName());
                        if (entry.isDirectory()) {
                            Files.createDirectories(outPath);
                        }
                        else {
                            Files.createDirectories(outPath.getParent());
                            // Overwrite existing files
                            Files.deleteIfExists(outPath);
                            Files.copy(tarFile.getInputStream(entry), outPath);
                        }
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    @SneakyThrows
    static void extractZipFiles(List<Path> zips, Path outDir) {
        for (var zip : zips) {
            System.out.println("Extracting " + zip);
            try (var zipFile = new org.apache.commons.compress.archivers.zip.ZipFile(zip.toFile())) {
                var entryList = Collections.list(zipFile.getEntries());
                entryList.forEach(entry -> {
                    try {
                        var outPath = outDir.resolve(entry.getName());
                        if (entry.isDirectory()) {
                            Files.createDirectories(outPath);
                        }
                        else {
                            Files.createDirectories(outPath.getParent());
                            // Overwrite existing files
                            Files.deleteIfExists(outPath);
                            try (var in = zipFile.getInputStream(entry)) {
                                Files.copy(in, outPath);
                            }
                        }
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    /**
     * Copies the contents of each of the subdirectories under inputDir, alphabetically sorted, to outDir. The subdirectories are copied recursively, so that the directory structure is preserved.
     * Existing files are overwritten.
     *
     * @param inputDir the directory containing the directories to copy
     * @param outDir   the directory to copy the directories to
     * @throws IOException if copying fails
     */
    static void copyDirectoriesInOrder(Path inputDir, Path outDir) throws IOException {
        try (var stream = Files.walk(inputDir)) {
            var dirs = stream
                .filter(Files::isDirectory)
                .filter(path -> path.getParent().equals(inputDir))
                .sorted()
                .toList();
            for (var dir : dirs) {
                copyDirectoryContents(dir, outDir);
            }
        }
    }

    static void copyDirectoryContents(Path src, Path dest) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(src)) {
            for (Path path : directoryStream) {
                File srcFile = path.toFile();
                File destFile = new File(dest.toFile(), srcFile.getName());
                if (srcFile.isDirectory()) {
                    FileUtils.copyDirectory(srcFile, destFile);
                }
                else {
                    FileUtils.copyFile(srcFile, destFile);
                }
            }
        }
    }
}
