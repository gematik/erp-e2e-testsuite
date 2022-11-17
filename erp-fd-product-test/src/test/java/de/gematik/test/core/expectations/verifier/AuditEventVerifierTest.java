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

package de.gematik.test.core.expectations.verifier;

import de.gematik.test.core.expectations.requirements.*;
import de.gematik.test.erezept.*;
import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.konnektor.commands.options.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.api.*;

@Slf4j
class AuditEventVerifierTest {

  private PharmacyActor pharmacy;

  @BeforeEach
  void setupReporter() {
    CoverageReporter.getInstance().startTestcase("not needed");
    val config = ErpConfiguration.create();
    pharmacy = new PharmacyActor("Am Flughafen");
    val pharmacyConfig = config.getPharmacyConfig(pharmacy.getName());
    val smartcards = config.getSmartcards();
    val smcb =
        smartcards.getSmcbByICCSN(
            pharmacyConfig.getSmcbIccsn(), pharmacyConfig.getCryptoAlgorithm());

    val useSmcb = UseSMCB.itHasAccessTo(smcb);
    pharmacy.can(useSmcb);
  }

  @Test
  void shouldDetectAllRepresentations() {
    val checksum = ExamEvidence.NO_UPDATES.getChecksum().orElseThrow();
    val verifier = AuditEventVerifier.builder().pharmacy(pharmacy).checksum(checksum).build();
    val testData =
        FhirTestResourceUtil.createErxAuditEventBundle(
            pharmacy.getTelematikId(), pharmacy.getCommonName(), checksum);
    for (Representation rep : Representation.values()) {
      verifier.contains(rep).apply(testData);
    }
    verifier
        .firstCorrespondsTo(Representation.PHARMACY_GET_TASK_SUCCESSFUL_WITH_CHECKSUM)
        .apply(testData);
  }
}
