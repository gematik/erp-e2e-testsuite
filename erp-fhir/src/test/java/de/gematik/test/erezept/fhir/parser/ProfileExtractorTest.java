/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.parser;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.parser.profiles.ProfileExtractor;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

@Slf4j
class ProfileExtractorTest {

  @Test
  void shouldExtractXmlProfiles() {
    val kbvBundleResources =
        ResourceUtils.getResourceFilesInDirectory("fhir/valid/kbv/1.0.2/bundle");
    val expectedProfile = "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2";

    kbvBundleResources.forEach(
        file -> {
          log.info(format("Extract profile from {0}", file.getName()));
          val content = ResourceUtils.readFileFromResource(file);
          val profile = ProfileExtractor.extractProfile(content);
          assertEquals(expectedProfile, profile.orElseThrow());
        });
  }

  @Test
  void shouldExtractJsonProfiles() {
    val basePath = "fhir/valid/erp/1.1.1/";
    val fileName = "Task.json";
    val content = ResourceUtils.readFileFromResource(basePath + fileName);
    val profile = ProfileExtractor.extractProfile(content);

    val expectedProfile = "https://gematik.de/fhir/StructureDefinition/ErxTask|1.1.1";
    assertTrue(profile.isPresent());
    assertEquals(expectedProfile, profile.orElseThrow());
  }
}
