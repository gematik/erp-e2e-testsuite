/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ErxAuditEventBundleRelationsTest extends ErpFhirParsingTest {

  private static final String VALID_BUNDLE_PATH =
      "fhir/valid/erp/1.6.0/xml/auditEvent/auditEventBundleByFD.xml";
  private static final String INVALID_BUNDLE_PATH =
      "fhir/invalid/erp/1.6/auditEvents/AuditEventBundleByFD.xml";
  private static ErxAuditEventBundle erxAuditEventBundle;
  private static ErxAuditEventBundle invalidErxAuditEventBundle;

  @BeforeAll
  static void prepare() {
    erxAuditEventBundle =
        parser.decode(
            ErxAuditEventBundle.class, ResourceLoader.readFileFromResource(VALID_BUNDLE_PATH));
    invalidErxAuditEventBundle =
        parser.decode(
            ErxAuditEventBundle.class, ResourceLoader.readFileFromResource(INVALID_BUNDLE_PATH));
  }

  @Test
  void shouldDetectNextRelation() {
    assertTrue(erxAuditEventBundle.hasNextRelation());
  }

  @Test
  void shouldDetectPreviousRelation() {
    assertTrue(erxAuditEventBundle.hasPreviousRelation());
  }

  @Test
  void shouldDetectFirstRelation() {
    assertTrue(erxAuditEventBundle.hasFirstRelation());
  }

  @Test
  void shouldDetectLastRelation() {
    assertTrue(erxAuditEventBundle.hasLastRelation());
  }

  @Test
  void shouldDetectSelfRelation() {
    assertTrue(erxAuditEventBundle.hasSelfRelation());
  }

  @Test
  void shouldDetectNoNextRelation() {
    assertFalse(invalidErxAuditEventBundle.hasNextRelation());
  }

  @Test
  void shouldDetectNoPreviousRelation() {
    assertFalse(invalidErxAuditEventBundle.hasPreviousRelation());
  }

  @Test
  void shouldDetectNoFirstRelation() {
    assertFalse(invalidErxAuditEventBundle.hasFirstRelation());
  }

  @Test
  void shouldDetectNoLastRelation() {
    assertFalse(invalidErxAuditEventBundle.hasLastRelation());
  }

  @Test
  void shouldDetectNoSelfRelation() {
    assertFalse(invalidErxAuditEventBundle.hasSelfRelation());
  }

  @Test
  void shouldDetectOffsetCorrectAtNext() {
    assertEquals(Optional.empty(), erxAuditEventBundle.getOffsetValueOfRelationLink("next"));
  }

  @Test
  void shouldDetectOffsetCorrectAtPrevious() {
    assertEquals(Optional.of(20), erxAuditEventBundle.getOffsetValueOfRelationLink("prev"));
  }

  @Test
  void shouldDetectOffsetCorrectAtFirst() {
    assertEquals(Optional.empty(), erxAuditEventBundle.getOffsetValueOfRelationLink("first"));
  }

  @Test
  void shouldDetectOffsetCorrectAtNonsense3() {
    assertEquals(Optional.empty(), erxAuditEventBundle.getOffsetValueOfRelationLink("nonsense3"));
  }

  @Test
  void shouldDetectOffsetCorrectAtNonsense() {
    assertEquals(
        Optional.empty(), invalidErxAuditEventBundle.getOffsetValueOfRelationLink("nonsense"));
  }

  @Test
  void shouldDetectOffsetCorrectAtLast() {
    assertEquals(Optional.of(40), erxAuditEventBundle.getOffsetValueOfRelationLink("last"));
  }
}
