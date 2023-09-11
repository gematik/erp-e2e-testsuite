/*
 * Copyright (c) 2023 gematik GmbH
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
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.parser.profiles.ProfileExtractor;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

  @ParameterizedTest
  @ValueSource(strings = {"<xml>", "alternative_json", ""})
  void shouldNotCrashOnCheckingSearchBundles(String content) {
    assertFalse(ProfileExtractor.isSearchSetOrCollection(content));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "erp/1.1.1/Parameters-accept-out.json",
        "erp/1.2.0/chargeitembundle/ea33a992-a214-11ed-a8fc-0242ac120002.xml"
      })
  void shouldDetectSearchSetsAndCollections(String resourcePath) {
    val base = "fhir/valid";
    val resource = format("{0}/{1}", base, resourcePath);
    val content = ResourceUtils.readFileFromResource(resource);
    assertTrue(ProfileExtractor.isSearchSetOrCollection(content));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "dav/ad80703d-8c62-44a3-b12b-2ea66eda0aa2.xml",
        "erp/1.2.0/receiptbundle/dffbfd6a-5712-4798-bdc8-07201eb77ab8.json",
        "kbv/1.1.0/bundle/1f339db0-9e55-4946-9dfa-f1b30953be9b.xml",
        "erp/1.2.0/autidevent/9361863d-fec0-4ba9-8776-7905cf1b0cfa.xml",
      })
  void shouldPassOtherResources(String resourcePath) {
    val base = "fhir/valid";
    val resource = format("{0}/{1}", base, resourcePath);
    val content = ResourceUtils.readFileFromResource(resource);
    assertFalse(ProfileExtractor.isSearchSetOrCollection(content));
  }
}
