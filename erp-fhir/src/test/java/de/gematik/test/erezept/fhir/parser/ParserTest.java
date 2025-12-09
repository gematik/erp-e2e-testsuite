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

package de.gematik.test.erezept.fhir.parser;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.parser.DataFormatException;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvAbgabedatenBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.EncodingUtil;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.RegExUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

@Slf4j
class ParserTest extends ErpFhirParsingTest {

  @Test
  void shouldParseOfficialKbvBundleResources() {
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
              PrescriptionId.from(RegExUtil.getPrescriptionIdFor(content).orElseThrow());

          val id = file.getName().split("\\.")[0]; // cut the file extension: filename == ID
          val bundleMetaProfile = kbvBundle.getFullMetaProfile();
          val bundleMetaProfileVersion = kbvBundle.getMetaProfileVersion();
          val prescriptionNamingSystem = kbvBundle.getPrescriptionId().getSystem();
          val entries = kbvBundle.getEntry();

          assertEquals(id, kbvBundle.getLogicalId());
          assertTrue(KbvItaErpStructDef.BUNDLE.matches(bundleMetaProfile));
          assertEquals("1.1.0", bundleMetaProfileVersion);
          assertEquals(ErpWorkflowNamingSystem.PRESCRIPTION_ID, prescriptionNamingSystem);
          assertEquals(expectedPrescriptionId, kbvBundle.getPrescriptionId());

          // profile cardinality is defined as 1..*
          assertFalse(entries.isEmpty());
        });
  }

  @Test
  void shouldParseResourceWithoutExpectedClass() {
    val id = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val content =
        ResourceLoader.readFileFromResource(format("fhir/valid/kbv/1.1.0/bundle/{0}.xml", id));

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
        ResourceLoader.readFileFromResource(format("fhir/valid/kbv/1.1.0/bundle/{0}.xml", id));
    val resource = parser.decode(content);
    assertTrue(resource.getId().contains(id));
    assertEquals(KbvErpBundle.class, resource.getClass());

    // now try json
    val contentJson = EncodingUtil.reEncode(parser, resource, EncodingType.JSON);
    val resource2 = parser.decode(contentJson);
    assertTrue(resource.getId().contains(id));
    assertEquals(KbvErpBundle.class, resource2.getClass());
  }

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldDecodeEmptyResource(String content) {
    val resource = assertDoesNotThrow(() -> parser.decode(EmptyResource.class, content));
    assertEquals(EmptyResource.class, resource.getClass());
  }

  static Stream<Arguments> shouldDecodeEmptyResource() {
    return Stream.of("", " ", "\t", "\n", "\r", "\r\n").map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("shouldDecodeEmptyResource")
  @NullSource
  void shouldThrowOnEmptyWhenExpectedSpecificResource(String content) {
    // this exception must be later on handled by the client
    assertThrows(DataFormatException.class, () -> parser.decode(ErxTask.class, content));
  }

  @Test
  void shouldDetectOperationOutcomeWhenExpectedEmptyResource() {
    val oo = new OperationOutcome();
    oo.addIssue()
        .setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
        .setCode(OperationOutcome.IssueType.INFORMATIONAL)
        .setDiagnostics("This is an empty resource");
    val content = parser.encode(oo, EncodingType.XML);

    // Note: should be DataFormatException or FhirCodecException; this will be fixed in bricks 0.8.0
    // this exception must be later on handled by the client
    assertThrows(ConfigurationException.class, () -> parser.decode(EmptyResource.class, content));
  }

  @ParameterizedTest
  @EnumSource(EncodingType.class)
  void shouldEncodeEmptyResource(EncodingType encodingType) {
    val content = assertDoesNotThrow(() -> parser.encode(new EmptyResource(), encodingType));
    assertTrue(content.isEmpty());
  }
}
