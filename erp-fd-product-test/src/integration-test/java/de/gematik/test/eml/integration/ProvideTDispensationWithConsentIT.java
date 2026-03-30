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

package de.gematik.test.eml.integration;

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleContainsLogFor;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.emlMedicationHasCategory;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.eml.tasks.CheckEpaOpProvideDispensation;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.arguments.MedicationTypeAndInsuranceComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Provide Teratogenic Dispensation with Consent Test")
@Tag("ProvideEmlDispensationAsTPrescription")
@Tag("EpaEml")
public class ProvideTDispensationWithConsentIT extends ErpTest {

  @Actor(name = "Günther Angermänn")
  private PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doc;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  private ErxAcceptBundle acceptance;

  public static Stream<Arguments> workflowAndInsuranceComposer() {
    return MedicationTypeAndInsuranceComposer.getComposer().create();
  }

  private final List<IQueryParameter> searchParams =
      IQueryParameter.search()
          .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
          .sortedBy("date", SortOrder.DESCENDING)
          .createParameter();

  @TestcaseId("EML_PROVIDE_T_DISPENSATION_WITH_CONSENT_DECISION_APPLY_01")
  @ParameterizedTest(
      name =
          "[{index}] -> für einen {1} eines {0} versicherten soll die zum Epa-Aktensystem gesendete"
              + " Dispensierung einer {2}- T Prescription überprüft werden")
  @DisplayName(
      "Es muss geprüft werden, dass die übermittelte Teratogenic Prescription an das Epa"
          + " Aktensystem den Werten der ausgestellten Teratogenic Prescription entspricht")
  @MethodSource("workflowAndInsuranceComposer")
  void submittTPrescriptionToEpaMock(
      InsuranceTypeDe insuranceType,
      PrescriptionFlowType expectedFlowTypeForDescription,
      String medicationType) {

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    patient.changePatientInsuranceType(insuranceType);

    val medication =
        KbvErpMedicationPZNFaker.builder(KbvItaErpVersion.V1_4_0)
            .withCategory(MedicationCategory.C_02)
            .withVaccine(false)
            .withPznMedication(PZN.from("19201712"), "Pomalidomid Accord 1 mg 21 x 1 Hartkapseln")
            .fake();

    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .asTPrescription(
                    KbvErpBundleFaker.builder()
                        .withMedication(medication)
                        .withDosageInstruction("1-0-3-5")
                        .toBuilder()));

    // verifies correct activated prescription and get KbvErpBundle for validation step
    val task = activation.getExpectedResponse();

    // performs the resource-content validation
    acceptance = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    pharmacy.performs(ClosePrescription.acceptedWith(acceptance));

    val medDispBundle =
        patient
            .performs(
                GetMedicationDispense.withQueryParams(
                    IQueryParameter.search()
                        .identifier(task.getPrescriptionId().asIdentifier())
                        .createParameter()))
            .getExpectedResponse();

    epaFhirChecker.attemptsTo(
        CheckEpaOpProvideDispensation.forDispensationWithAdditionalVerifier(
            medDispBundle,
            pharmacy.getTelematikId(),
            task.getPrescriptionId(),
            List.of(emlMedicationHasCategory(MedicationCategory.C_02))));

    val auditEvents = patient.performs(DownloadAuditEvent.withQueryParams(searchParams));
    patient.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLogFor(
                    task.getPrescriptionId(),
                    "Die Medikamentenabgabe wurde in die Patientenakte übertragen"))
            .isCorrect());
  }
}
