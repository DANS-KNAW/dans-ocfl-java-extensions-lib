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

import java.io.IOException;
import java.util.stream.Collectors;

public class VersionPropertiesExtension implements OcflExtension {
    public static final String EXTENSION_NAME = "object-version-properties";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String VERSION_PROPERTIES_FILE = "object_version_properties.json";

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public void validate(ValidationContext context) {
        var storage = context.getStorage();
        var extensionPath = context.getExtensionPath();
        var filePath = extensionPath + "/" + VERSION_PROPERTIES_FILE;

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
}
