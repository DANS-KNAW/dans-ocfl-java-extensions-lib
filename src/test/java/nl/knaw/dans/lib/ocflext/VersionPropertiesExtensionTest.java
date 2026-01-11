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

import io.ocfl.api.model.ValidationCode;
import io.ocfl.core.extension.ValidationContext;
import io.ocfl.core.storage.common.Listing;
import io.ocfl.core.storage.common.Storage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VersionPropertiesExtensionTest {

    @Test
    public void onValidate_should_not_report_errors_when_file_is_valid() {
        var extension = new VersionPropertiesExtension();
        var context = Mockito.mock(ValidationContext.class);
        var storage = Mockito.mock(Storage.class);
        var json = """
            {
            "v1": {},
            "v2": {"key": "value"}}""";

        when(context.getStorage()).thenReturn(storage);
        when(context.getExtensionPath()).thenReturn("extensions/object-version-properties");
        when(context.getObjectRootPath()).thenReturn("");
        when(storage.fileExists(any())).thenReturn(true);
        when(storage.readToString(any())).thenReturn(json);
        when(storage.listDirectory("")).thenReturn(List.of(
            Listing.directory("v1"),
            Listing.directory("v2")
        ));

        extension.validate(context);

        verify(context, Mockito.never()).addIssue(any(), any(), any());
    }

    @Test
    public void validate_should_report_error_when_file_is_missing() {
        var extension = new VersionPropertiesExtension();
        var context = Mockito.mock(ValidationContext.class);
        var storage = Mockito.mock(Storage.class);

        when(context.getStorage()).thenReturn(storage);
        when(context.getExtensionPath()).thenReturn("extensions/object-version-properties");
        when(storage.fileExists(any())).thenReturn(false);

        extension.validate(context);

        verify(context).addIssue(eq(ValidationCode.EXTENSION_ERROR), contains("missing"));
    }

    @Test
    public void validate_should_report_error_when_version_is_missing_in_json() {
        var extension = new VersionPropertiesExtension();
        var context = Mockito.mock(ValidationContext.class);
        var storage = Mockito.mock(Storage.class);
        var json = """
            {"v1": {}}""";

        when(context.getStorage()).thenReturn(storage);
        when(context.getExtensionPath()).thenReturn("extensions/object-version-properties");
        when(context.getObjectRootPath()).thenReturn("");
        when(storage.fileExists(any())).thenReturn(true);
        when(storage.readToString(any())).thenReturn(json);
        when(storage.listDirectory("")).thenReturn(List.of(
            Listing.directory("v1"),
            Listing.directory("v2")
        ));

        extension.validate(context);

        verify(context).addIssue(eq(ValidationCode.EXTENSION_ERROR), contains("missing entry for version v2"));
    }

    @Test
    public void validate_should_report_error_when_json_is_malformed() {
        var extension = new VersionPropertiesExtension();
        var context = Mockito.mock(ValidationContext.class);
        var storage = Mockito.mock(Storage.class);
        var json = """
            {"v1": {}""";

        when(context.getStorage()).thenReturn(storage);
        when(context.getExtensionPath()).thenReturn("extensions/object-version-properties");
        when(storage.fileExists(any())).thenReturn(true);
        when(storage.readToString(any())).thenReturn(json);

        extension.validate(context);

        verify(context).addIssue(eq(ValidationCode.EXTENSION_ERROR), contains("not well-formed JSON"));
    }
}
