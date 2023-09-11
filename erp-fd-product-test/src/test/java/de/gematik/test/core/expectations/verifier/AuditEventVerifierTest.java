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

package de.gematik.test.core.expectations.verifier;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.ErpConfiguration;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent.Representation;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmChecksum;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    val smcb = smartcards.getSmcbByICCSN(pharmacyConfig.getSmcbIccsn());

    val useSmcb = UseSMCB.itHasAccessTo(smcb);
    pharmacy.can(useSmcb);
  }

  @Test
    // todo zugehöriges Ticket: TSERP-171
  void shouldDetectAllRepresentations() {
    val checksum =
            VsdmExamEvidence.builder(VsdmExamEvidenceResult.NO_UPDATES)
                    .checksum(VsdmChecksum.builder("X12345678").build())
                    .build()
                    .getChecksum()
                    .orElseThrow();
    val verifier = AuditEventVerifier.builder().pharmacy(pharmacy).checksum(checksum).build();
    val testData =
            FhirTestResourceUtil.createErxAuditEventBundle(
            pharmacy.getTelematikId(), pharmacy.getCommonName(), checksum);
    for (Representation rep : Representation.values()) {
      verifier.contains(rep).apply(testData);
    }
    verifier.firstCorrespondsTo(Representation.PHARMACY_GET_TASK_SUCCESSFUL).apply(testData);
  }
}
