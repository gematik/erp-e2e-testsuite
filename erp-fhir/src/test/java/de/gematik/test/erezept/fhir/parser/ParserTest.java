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
 */

package de.gematik.test.erezept.fhir.parser;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvAbgabedatenBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.EncodingUtil;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.RegExUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

@Slf4j
class ParserTest extends ErpFhirParsingTest {

  @Test
  void shouldParseOfficialOldKbvBundleResources() {
    val kbvBundleResources =
        ResourceLoader.getResourceFilesInDirectory("fhir/valid/kbv/1.0.2/bundle");

    kbvBundleResources.forEach(
        file -> {
          log.trace("Parse KBV Bundle {}", file.getName());
          val content = ResourceLoader.readString(file);
          val kbvBundle = parser.decode(KbvErpBundle.class, content);
          assertNotNull(kbvBundle);

          // get the prescription ID from raw content via RegEx
          val expectedPrescriptionId =
              new PrescriptionId(
                  ErpWorkflowNamingSystem.PRESCRIPTION_ID,
                  RegExUtil.getPrescriptionId(content).orElseThrow());

          val id = file.getName().split("\\.")[0]; // cut the file extension: filename == ID
          val bundleMetaProfile = kbvBundle.getFullMetaProfile();
          val bundleMetaProfileVersion = kbvBundle.getMetaProfileVersion();
          val prescriptionNamingSystem = kbvBundle.getIdentifier().getSystem();
          val entries = kbvBundle.getEntry();

          assertEquals(id, kbvBundle.getLogicalId());
          assertTrue(KbvItaErpStructDef.BUNDLE.matches(bundleMetaProfile));
          assertEquals("1.0.2", bundleMetaProfileVersion);
          assertEquals(
              ErpWorkflowNamingSystem.PRESCRIPTION_ID.getCanonicalUrl(), prescriptionNamingSystem);
          assertEquals(expectedPrescriptionId, kbvBundle.getPrescriptionId());

          // profile cardinality is defined as 1..*
          assertFalse(entries.isEmpty());
        });
  }

  @Test
  void shouldParseOfficialNewKbvBundleResources() {
    val kbvBundleResources =
        ResourceLoader.getResourceFilesInDirectory("fhir/valid/kbv/1.1.0/bundle");

    kbvBundleResources.forEach(
        file -> {
          log.trace("Parse KBV Bundle {}", file.getName());
          val content = ResourceLoader.readString(file);
          val kbvBundle = parser.decode(KbvErpBundle.class, content);
          assertNotNull(kbvBundle);

          // get the prescription ID from raw content via RegEx
          val expectedPrescriptionId =
              new PrescriptionId(RegExUtil.getPrescriptionIdFor121(content).orElseThrow());

          val id = file.getName().split("\\.")[0]; // cut the file extension: filename == ID
          val bundleMetaProfile = kbvBundle.getFullMetaProfile();
          val bundleMetaProfileVersion = kbvBundle.getMetaProfileVersion();
          val prescriptionNamingSystem = kbvBundle.getPrescriptionId().getSystem();
          val entries = kbvBundle.getEntry();

          assertEquals(id, kbvBundle.getLogicalId());
          assertTrue(KbvItaErpStructDef.BUNDLE.matches(bundleMetaProfile));
          assertEquals("1.1.0", bundleMetaProfileVersion);
          assertEquals(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121, prescriptionNamingSystem);
          assertEquals(expectedPrescriptionId, kbvBundle.getPrescriptionId());

          // profile cardinality is defined as 1..*
          assertFalse(entries.isEmpty());
        });
  }

  @Test
  void shouldParseResourceWithoutExpectedClass() {
    val id = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val content =
        ResourceLoader.readFileFromResource(format("fhir/valid/kbv/1.0.2/bundle/{0}.xml", id));

    val r1 = parser.decode(content, EncodingType.XML);
    assertTrue(r1.getId().contains(id));

    val r2 = parser.decode(content);
    assertTrue(r2.getId().contains(id));
  }

  @Test
  void shouldGetConcreteDavTypeFromTypeHints() {
    val id = "ad80703d-8c62-44a3-b12b-2ea66eda0aa2";
    List.of(EncodingType.XML, EncodingType.JSON)
        .forEach(
            encoding -> {
              val content =
                  ResourceLoader.readFileFromResource(
                      format("fhir/valid/dav/{0}.{1}", id, encoding.toFileExtension()));
              val resource = parser.decode(content);
              assertTrue(resource.getId().contains(id));
              assertEquals(DavPkvAbgabedatenBundle.class, resource.getClass());
            });
  }

  @Test
  void shouldGetConcreteKbvTypeFromTypeHints() {
    val id = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val content =
        ResourceLoader.readFileFromResource(format("fhir/valid/kbv/1.0.2/bundle/{0}.xml", id));
    val resource = parser.decode(content);
    assertTrue(resource.getId().contains(id));
    assertEquals(KbvErpBundle.class, resource.getClass());

    // now try json
    val contentJson = EncodingUtil.reEncode(parser, resource, EncodingType.JSON);
    val resource2 = parser.decode(contentJson);
    assertTrue(resource.getId().contains(id));
    assertEquals(KbvErpBundle.class, resource2.getClass());
  }
}
