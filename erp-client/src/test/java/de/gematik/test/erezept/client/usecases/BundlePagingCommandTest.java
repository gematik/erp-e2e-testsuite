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

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BundlePagingCommandTest extends ErpFhirParsingTest {

  private static ErxAuditEventBundle erxAuditEventBundle;

  @BeforeAll
  static void prepare() {
    erxAuditEventBundle =
        parser.decode(
            ErxAuditEventBundle.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/erp/1.2.0/auditeventbundle/41f94920-14b5-426a-8859-d045270e63a2.xml"));
  }

  @Test
  void shouldGetNext() {
    val command = BundlePagingCommand.getNextFrom(erxAuditEventBundle);
    assertNotNull(command);
    assertNotNull(command.queryParameters);
    assertNotNull(command.getHeaderParameters());
  }

  @Test
  void shouldGetSelf() {
    val command = BundlePagingCommand.getSelfFrom(erxAuditEventBundle);
    assertNotNull(command);
    assertNotNull(command.queryParameters);
    assertNotNull(command.getHeaderParameters());
  }

  @Test
  void shouldGetPrevious() {
    val command = BundlePagingCommand.getPreviousFrom(erxAuditEventBundle);
    assertNotNull(command);
    assertNotNull(command.queryParameters);
    assertNotNull(command.getHeaderParameters());
    assertEquals(Optional.empty(), command.getRequestBody());
  }

  @Test
  void shouldGetFromConcreteUrl() {
    val command =
        BundlePagingCommand.fromConcreteURL(
            ErxAuditEventBundle.class,
            "https://erp-dev.zentral.erp.splitdns.ti-dienste.de/AuditEvent?_count=50&_id=gt01eb9373-789e-7db8-0000-000000000000");
    assertNotNull(command);
    assertNotNull(command.queryParameters);
    assertNotNull(command.getHeaderParameters());
  }
}
