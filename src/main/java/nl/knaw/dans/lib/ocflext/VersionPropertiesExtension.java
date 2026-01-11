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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ocfl.api.model.ValidationCode;
import io.ocfl.core.extension.OcflExtension;
import io.ocfl.core.extension.ValidationContext;
import io.ocfl.core.storage.common.Listing;
import io.ocfl.core.storage.common.Storage;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

public class VersionPropertiesExtension implements OcflExtension {
    public static final String EXTENSION_NAME = "object-version-properties";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String VERSION_PROPERTIES_FILE = "object_version_properties.json";

    private interface ChecksumCalculator {
        String calculate(InputStream is) throws IOException;
    }

    private static final Map<String, ChecksumCalculator> SUPPORTED_ALGORITHMS = Map.of(
        "sha256", DigestUtils::sha256Hex,
        "sha512", DigestUtils::sha512Hex,
        "md5", DigestUtils::md5Hex,
        "sha1", DigestUtils::sha1Hex
    );

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public void validate(ValidationContext context) {
        var storage = context.getStorage();
        var extensionPath = context.getExtensionPath();
        var filePath = extensionPath + "/" + VERSION_PROPERTIES_FILE;

        validateSidecar(context, extensionPath, filePath);
        validateVersionProperties(context, storage, filePath);
    }

    private void validateVersionProperties(ValidationContext context, Storage storage, String filePath) {
        if (storage.fileExists(filePath)) {
            try {
                var json = storage.readToString(filePath);
                var root = objectMapper.readTree(json);

                if (!root.isObject()) {
                    context.addIssue(ValidationCode.EXTENSION_ERROR, String.format("Extension file %s must be a JSON object", filePath));
                }
                else {
                    var versionNames = storage.listDirectory(context.getObjectRootPath()).stream()
                        .filter(Listing::isDirectory)
                        .map(Listing::getRelativePath)
                        .filter(name -> name.matches("v\\d+"))
                        .collect(Collectors.toSet());

                    for (var versionName : versionNames) {
                        if (!root.has(versionName)) {
                            context.addIssue(ValidationCode.EXTENSION_ERROR, String.format("Extension file %s is missing entry for version %s", filePath, versionName));
                        }
                        else if (!root.get(versionName).isObject()) {
                            context.addIssue(ValidationCode.EXTENSION_ERROR, String.format("Extension file %s entry for version %s must be a JSON object", filePath, versionName));
                        }
                    }
                }
            }
            catch (IOException e) {
                context.addIssue(ValidationCode.EXTENSION_ERROR, String.format("Extension file %s could not be read or is not well-formed JSON: %s", filePath, e.getMessage()));
            }
        }
        else {
            context.addIssue(ValidationCode.EXTENSION_ERROR, String.format("Extension file %s is missing", filePath));
        }
    }

    private void validateSidecar(ValidationContext context, String extensionPath, String filePath) {
        var storage = context.getStorage();
        var foundSidecar = false;

        for (var algorithm : SUPPORTED_ALGORITHMS.keySet()) {
            var sidecarPath = extensionPath + "/" + VERSION_PROPERTIES_FILE + "." + algorithm;
            if (storage.fileExists(sidecarPath)) {
                foundSidecar = true;
                if (storage.fileExists(filePath)) {
                    validateChecksum(context, storage, filePath, sidecarPath, algorithm);
                }
                break;
            }
        }

        if (!foundSidecar) {
            context.addIssue(ValidationCode.EXTENSION_ERROR,
                String.format("Sidecar file for %s is missing. Supported algorithms: %s", VERSION_PROPERTIES_FILE, SUPPORTED_ALGORITHMS));
        }
    }

    private void validateChecksum(ValidationContext context, Storage storage, String filePath, String sidecarPath, String algorithm) {
        try {
            var expectedChecksum = storage.readToString(sidecarPath).split("\\s+")[0];
            var actualChecksum = calculateChecksum(storage, filePath, algorithm);

            if (!expectedChecksum.equalsIgnoreCase(actualChecksum)) {
                context.addIssue(ValidationCode.EXTENSION_ERROR, String.format("Sidecar file %s contains incorrect checksum", sidecarPath));
            }
        }
        catch (IOException e) {
            context.addIssue(ValidationCode.EXTENSION_ERROR, String.format("Error reading file %s or %s: %s", filePath, sidecarPath, e.getMessage()));
        }
    }

    private String calculateChecksum(Storage storage, String filePath, String algorithm) throws IOException {
        try (var inputStream = storage.read(filePath)) {
            var calculator = SUPPORTED_ALGORITHMS.get(algorithm.toLowerCase());
            if (calculator == null) {
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
            }
            return calculator.calculate(inputStream);
        }
    }
}
