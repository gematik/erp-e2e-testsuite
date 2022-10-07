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
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.EncodingUtil;
import de.gematik.test.erezept.fhir.testutil.RegExUtil;
import de.gematik.test.erezept.fhir.util.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

@Slf4j
class ParserTest extends ParsingTest {

  @Test
  // TODO: still required? Remove or move to ErxAuditEventTest
  void shouldParseErxAuditEvents() {
    val basePath = "fhir/valid/erp/";
    val auditEvents = List.of("AuditEvent_01.json");

    auditEvents.stream()
        .map(filename -> basePath + filename)
        .map(ResourceUtils::readFileFromResource)
        .forEach(
            content -> {
              val auditEvent = parser.decode(ErxAuditEvent.class, content);
              assertNotNull(auditEvent, "Valid ErxAuditEvent must be parseable");

              val expectedAction = RegExUtil.getAuditEventAction(content).orElseThrow();
              val actualAction = auditEvent.getAction();
              assertEquals(expectedAction, actualAction, "AuditEvent Action does not match");
            });
  }

  @Test
  void roundtripAllAuditEvents() {
    val basePath = "fhir/valid/erp/";
    val auditEvents = List.of("AuditEvent_01.json");

    auditEvents.stream()
        .map(filename -> basePath + filename)
        .forEach(
            filepath -> {
              EncodingUtil.validateRoundtripEncoding(parser, filepath, ErxAuditEvent.class);
            });
  }

  @Test
  void roundtripAllTasks() {
    val basePath = "fhir/valid/erp/";
    val auditEvents =
        List.of(
            "Task_01.xml",
            "Task_02.xml",
            "Task_03.xml",
            "Task_03.json",
            "Task_04.xml",
            "Task_05.xml",
            "Task_06.xml");

    auditEvents.stream()
        .map(filename -> basePath + filename)
        .forEach(
            filepath -> {
              EncodingUtil.validateRoundtripEncoding(parser, filepath, ErxTask.class);
            });
  }

  @Test
  void shouldParseOfficialKbvBundleResources() {
    val kbvBundleResources = ResourceUtils.getResourceFilesInDirectory("fhir/valid/kbv/bundle");

    kbvBundleResources.forEach(
        file -> {
          log.info(format("Parse KBV Bundle {0}", file.getName()));
          val content = ResourceUtils.readFileFromResource(file);
          val kbvBundle = parser.decode(KbvErpBundle.class, content);
          assertNotNull(kbvBundle);

          // get the prescription ID from raw content via RegEx
          val expectedPrescriptionId =
              new PrescriptionId(RegExUtil.getPrescriptionId(content).orElseThrow());

          val id = file.getName().split("\\.")[0]; // cut the file extension: filename == ID
          val bundleMetaProfile = kbvBundle.getFullMetaProfile();
          val bundleMetaProfileVersion = kbvBundle.getMetaProfileVersion();
          val prescriptionNamingSystem = kbvBundle.getIdentifier().getSystem();
          val entries = kbvBundle.getEntry();

          assertEquals(id, kbvBundle.getLogicalId());
          assertEquals(ErpStructureDefinition.KBV_BUNDLE.getCanonicalUrl(), bundleMetaProfile);
          assertEquals("1.0.2", bundleMetaProfileVersion);
          assertEquals(ErpNamingSystem.PRESCRIPTION_ID.getCanonicalUrl(), prescriptionNamingSystem);
          assertEquals(expectedPrescriptionId, kbvBundle.getPrescriptionId());

          // profile cardinality is defined as 1..*
          assertTrue(entries.size() > 0);
        });
  }

  @Test
  void roundtripAllKbvBundles() {
    val basePath = "fhir/valid/kbv/bundle/";
    val kbvBundle = ResourceUtils.getResourceFilesInDirectory(basePath);

    kbvBundle.stream()
        .map(file -> basePath + file.getName())
        .forEach(
            filepath -> {
              EncodingUtil.validateRoundtripEncoding(parser, filepath, KbvErpBundle.class);
            });
  }
}
