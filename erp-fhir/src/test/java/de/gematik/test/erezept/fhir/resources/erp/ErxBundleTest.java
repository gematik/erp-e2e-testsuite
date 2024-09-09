/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ErxBundleTest extends ParsingTest {

  private static final String VALID_BUNDLE_PATH =
      "fhir/valid/erp/1.2.0/auditeventbundle/2d4ba5fa-d0ff-4b59-b1c4-8849bd83a971.json";
  private static final String INVALID_BUNDLE_PATH = "fhir/invalid/erp/AuditEventBundle.json";
  private static ErxAuditEventBundle erxAuditEventBundle;
  private static ErxAuditEventBundle invaliErxAuditEventBundle;

  @BeforeAll
  static void prepare() {
    erxAuditEventBundle =
        parser.decode(
            ErxAuditEventBundle.class, ResourceLoader.readFileFromResource(VALID_BUNDLE_PATH));
    invaliErxAuditEventBundle =
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
    assertFalse(invaliErxAuditEventBundle.hasNextRelation());
  }

  @Test
  void shouldDetectNoPreviousRelation() {
    assertFalse(invaliErxAuditEventBundle.hasPreviousRelation());
  }

  @Test
  void shouldDetectNoFirstRelation() {
    assertFalse(invaliErxAuditEventBundle.hasFirstRelation());
  }

  @Test
  void shouldDetectNoLastRelation() {
    assertFalse(invaliErxAuditEventBundle.hasLastRelation());
  }

  @Test
  void shouldDetectNoSelfRelation() {
    assertFalse(invaliErxAuditEventBundle.hasSelfRelation());
  }
}
