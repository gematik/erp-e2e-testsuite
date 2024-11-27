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

package de.gematik.test.eml.integration;

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleContainsLogFor;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.*;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.eml.tasks.CheckEpaOpProvidePrescriptionWithTask;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.TelematikID;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.time.LocalDate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Provide Prescription with Consent Test")
@Tag("ProvideEmlPrescription")
@Tag("EpaEml")
public class PrescriptionProvideWithConsentIT extends ErpTest {

  public static Stream<Arguments> workflowAndMedicationComposer() {
    return WorkflowAndMedicationComposer.workflowAndMedicationComposer().create();
  }

  @Actor(name = "Günther Angermänn")
  private PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doc;

  @TestcaseId("EML_Provide_Prescription_with_Consent_01")
  @ParameterizedTest(
      name =
          "[{index}] -> für einen Flow Type {2} sollen die zum Epa-Aktensystem gesendete"
              + " Prescription mit {3} überprüft werden")
  @DisplayName(
      "Es muss geprüft werden, dass die übermittelte Prescription an das Epa Aktensystem den Werten"
          + " der Prescription entspricht")
  @MethodSource("workflowAndMedicationComposer")
  void checkSubmittedPrescriptionInformation(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowTypeForDescription,
      String medicationType) {

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    patient.changePatientInsuranceType(insuranceType);
    // todo
    // patient.attemptsTo(EmlProvidePrescriptionApply.forKvnr(patient.getKvnr()));

    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(getMedication(medicationType))
                        .toBuilder()));

    // verifies correct activated prescription and get KbvErpBundle for validation step
    val task = activation.getExpectedResponse();

    val prescr =
        patient.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));

    // performs the resource-content validation
    epaFhirChecker.attemptsTo(
        CheckEpaOpProvidePrescriptionWithTask.forPrescription(
            prescr.getExpectedResponse().getKbvBundle().orElseThrow(),
            TelematikID.from(SafeAbility.getAbility(doc, UseSMCB.class).getTelematikID()),
            TelematikID.from(doc.getHbaTelematikId())));

    // patient checks auditEvent content
    val searchParams =
        IQueryParameter.search()
            .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
            .sortedBy("date", SortOrder.DESCENDING)
            .createParameter();
    val auditEvents = patient.performs(DownloadAuditEvent.withQueryParams(searchParams));

    patient.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLogFor(
                    prescr.getExpectedResponse().getTask().getPrescriptionId(),
                    "Die Verordnung wurde in die Patientenakte übertragen"))
            .isCorrect());

    // cleanUp
    patient.performs(TaskAbort.asPatient(task));
  }

  private KbvErpMedication getMedication(String medicationType) {
    return switch (medicationType) {
      case MEDICATION_PZN -> KbvErpMedicationPZNFaker.builder().fake();
      case MEDICATION_INGREDIENT -> KbvErpMedicationIngredientFaker.builder().fake();
      case MEDICATION_COMPOUNDING -> KbvErpMedicationCompoundingFaker.builder().fake();
      case MEDICATION_FREITEXT -> KbvErpMedicationFreeTextBuilder.builder()
          .freeText("Erp-To-Epa E2E Test")
          .build();
      default -> throw new IllegalArgumentException("Unknown medication type: " + medicationType);
    };
  }
}
