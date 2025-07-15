/*
 * Copyright 2025 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.extensions.erp;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UrlType;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class DeepLink {

  private final String value;

  public Extension asExtension() {
    val ext = new Extension(ErpWorkflowStructDef.DEEP_LINK.getCanonicalUrl());
    ext.setValue(new UrlType(value));
    return ext;
  }

  public static DeepLink from(String link) {
    return new DeepLink(link);
  }

  public static DeepLink random() {
    val url = GemFaker.getFaker().internet().url();
    val code = GemFaker.getFaker().code().asin();
    val link = format("{0}?code={1}", url, code);
    return from(link);
  }
}
