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

import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.testutil.EncodingUtil;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.RegExUtil;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
class ParserTest extends ParsingTest {

  @Test
  // TODO: still required? Remove or move to ErxAuditEventTest
  void shouldParseErxAuditEvents() {
    val basePath = "fhir/valid/erp/1.1.1/";
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
  @Disabled("cost too much test-time and is not very useful")
  void roundtripAllAuditEvents() {
    val basePath = "fhir/valid/erp/1.1.1/";
    val auditEvents = List.of("AuditEvent_01.json");

    auditEvents.stream()
        .map(filename -> basePath + filename)
        .forEach(
            filepath -> {
              EncodingUtil.validateRoundtripEncoding(parser, filepath, ErxAuditEvent.class);
            });
  }

  @Test
  @Disabled("cost too much test-time and is not very useful")
  void roundtripAllTasks() {
    val basePath = "fhir/valid/erp/1.1.1/";
    val tasks =
        List.of(
            "Task_01.xml",
            "Task_02.xml",
            "Task_03.xml",
            "Task_03.json",
            "Task_04.xml",
            "Task_05.xml",
            "Task_06.xml");

    tasks.stream()
        .map(filename -> basePath + filename)
        .forEach(
            filepath -> {
              EncodingUtil.validateRoundtripEncoding(parser, filepath, ErxTask.class);
            });
  }

  @Test
  void shouldParseOfficialKbvBundleResources() {
    val kbvBundleResources =
        ResourceUtils.getResourceFilesInDirectory("fhir/valid/kbv/1.0.2/bundle");

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
          assertTrue(KbvItaErpStructDef.BUNDLE.match(bundleMetaProfile));
          assertEquals("1.0.2", bundleMetaProfileVersion);
          assertEquals(
              ErpWorkflowNamingSystem.PRESCRIPTION_ID.getCanonicalUrl(), prescriptionNamingSystem);
          assertEquals(expectedPrescriptionId, kbvBundle.getPrescriptionId());

          // profile cardinality is defined as 1..*
          assertTrue(entries.size() > 0);
        });
  }

  @Test
  @Disabled("cost too much test-time and is not very useful")
  void roundtripAllKbvBundles() {
    val basePath = "fhir/valid/kbv/1.0.2/bundle/";
    val kbvBundle = ResourceUtils.getResourceFilesInDirectory(basePath);

    kbvBundle.stream()
        .map(file -> basePath + file.getName())
        .forEach(
            filepath -> {
              EncodingUtil.validateRoundtripEncoding(parser, filepath, KbvErpBundle.class);
            });
  }

  @Test
  void shouldNotAcceptNullOnValidate() {
    assertThrows(NullPointerException.class, () -> parser.validate(null)); // null on pupose!
    assertThrows(NullPointerException.class, () -> parser.isValid(null)); // null on pupose!
  }

  @Test
  void shouldNotAcceptNullOnDecode() {
    assertThrows(
        NullPointerException.class,
        () -> parser.decode(KbvErpBundle.class, null)); // null on pupose!
    assertThrows(
        NullPointerException.class,
        () -> parser.decode(KbvErpMedication.class, null, EncodingType.XML)); // null on pupose!
    assertThrows(NullPointerException.class, () -> parser.decode(null)); // null on pupose!
    assertThrows(
        NullPointerException.class,
        () -> parser.decode(null, EncodingType.JSON)); // null on pupose!
  }

  @Test
  void shouldNotAcceptNullOnEncode() {
    assertThrows(NullPointerException.class, () -> parser.encode(null, EncodingType.XML));
    assertThrows(NullPointerException.class, () -> parser.encode(null, EncodingType.JSON, true));
  }
}
