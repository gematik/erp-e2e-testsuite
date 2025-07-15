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

import static de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe.GKV;
import static de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe.PKV;
import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleContainsLogFor;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvidePrescriptionVerifier.emlPractitionerHasHbaTelematikId;
import static de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType.FLOW_TYPE_160;
import static de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType.FLOW_TYPE_169;
import static de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType.FLOW_TYPE_200;
import static de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType.FLOW_TYPE_209;
import static de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind.DIRECT_ASSIGNMENT;
import static de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind.PHARMACY_ONLY;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.eml.tasks.CheckErpDoesNotProvidePrescriptionToEpa;
import de.gematik.test.eml.tasks.LoadAndValidateProvidePrescription;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.GetPrescriptionById;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.TaskAbort;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.time.LocalDate;
import java.util.List;
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
@DisplayName("Provide Prescription without HBAs Telematik-ID Test")
@Tag("ProvideEmlPrescription")
@Tag("EpaEml")
public class ProvidePrescWithoutHbaTelematikID extends ErpTest {

  @Actor(name = "Günther Angermänn")
  private PatientActor patient;

  @Actor(name = "Michael Meierhofer")
  private DoctorActor docNoTelematikIDNotListed;

  @Actor(name = "Norbert Neuland")
  private DoctorActor docWithoutTelematikId;

  public static Stream<Arguments> workflowComposer() {
    return ArgumentComposer.composeWith()
        .arguments(GKV, PHARMACY_ONLY, FLOW_TYPE_160)
        .arguments(GKV, DIRECT_ASSIGNMENT, FLOW_TYPE_169)
        .arguments(PKV, PHARMACY_ONLY, FLOW_TYPE_200)
        .arguments(PKV, DIRECT_ASSIGNMENT, FLOW_TYPE_209)
        .create();
  }

  private static List<IQueryParameter> getQueryParameters() {
    return IQueryParameter.search()
        .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
        .sortedBy("date", SortOrder.DESCENDING)
        .createParameter();
  }

  @TestcaseId("EML_PROVIDE_PRESCRIPTION_WITHOUT_TELEMATIK_ID_01")
  @ParameterizedTest(
      name =
          "[{index}] -> eine aktivierte Prescription für einen {0}-Versicherten als {1} (Workflow:"
              + " {2}) soll ohne HBA-TelematikId an das Epa Aktensystem übermittelt werden, da der"
              + " Erp-FD anhand der SequenceNumber des Enc Certificates die TelematikId in einer"
              + " Mappingtabelle ermittelt und ergänzt")
  @DisplayName(
      "Es muss geprüft werden, dass eine Prescription die ohne HBA-TelematikId signiert wurde an"
          + " das Epa Aktensystem übermittelt wird")
  @MethodSource("workflowComposer")
  void telematikIdShouldBeResearchedByFD(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowTypeForDescription) {

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    patient.changePatientInsuranceType(insuranceType);
    // todo activate if PUT is activated
    // patient.attemptsTo(EmlProvidePrescriptionApply.forKvnr(patient.getKvnr()));

    val activation =
        docWithoutTelematikId.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(KbvErpBundleFaker.builder().toBuilder()));

    val task = activation.getExpectedResponse();

    val prescr =
        patient.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));

    val validatorList =
        List.of(emlPractitionerHasHbaTelematikId(docWithoutTelematikId.getHbaTelematikId()));

    epaFhirChecker.attemptsTo(
        LoadAndValidateProvidePrescription.withValidator(validatorList)
            .forPrescription(task.getPrescriptionId()));

    // patient checks auditEvent content

    val auditEvents = patient.performs(DownloadAuditEvent.withQueryParams(getQueryParameters()));
    val prescId = prescr.getExpectedResponse().getTask().getPrescriptionId();
    patient.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLogFor(
                    prescId, "Die Verordnung wurde in die Patientenakte übertragen"))
            .isCorrect());

    // cleanup
    docWithoutTelematikId.performs(
        TaskAbort.asLeistungserbringer(activation.getExpectedResponse()));
  }

  @TestcaseId("EML_PROVIDE_PRESCRIPTION_WITHOUT_TELEMATIK_ID_02")
  @ParameterizedTest(
      name =
          "[{index}] -> eine aktivierte Prescription für einen {0}-Versicherten als {1} (Workflow:"
              + " {2}) ohne HBA-TelematikId soll NICHT an das Epa Aktensystem übermittelt werden,"
              + " da der Erp-FD anhand der SequenceNumber des Enc Certificates die TelematikId"
              + " NICHT in einer Mappingtabelle ermittelt kann und entsprechend auch NICHT "
              + " ergänzt")
  @DisplayName(
      "Es muss geprüft werden, dass eine Prescription die ohne HBA-TelematikId signiert wurde NICHT"
          + " an das Epa Aktensystem übermittelt wird, da der entsprechende Eintrag in der"
          + " vereinbarten Mappingtabelle fehlt")
  @MethodSource("workflowComposer")
  void prescriptionShouldNoBeTransferredToFD() {
    InsuranceTypeDe insuranceType = InsuranceTypeDe.GKV;
    PrescriptionAssignmentKind assignmentKind = DIRECT_ASSIGNMENT;
    PrescriptionFlowType expectedFlowTypeForDescription = PrescriptionFlowType.FLOW_TYPE_160;

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);

    patient.changePatientInsuranceType(insuranceType);
    // todo activate if PUT is activated
    // patient.attemptsTo(EmlProvidePrescriptionApply.forKvnr(patient.getKvnr()));

    val activation =
        docNoTelematikIDNotListed.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(KbvErpBundleFaker.builder().toBuilder()));

    // verifies correct activated prescription and get KbvErpBundle for validation step
    val task = activation.getExpectedResponse();
    val prescr =
        patient.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));

    epaFhirChecker.attemptsTo(CheckErpDoesNotProvidePrescriptionToEpa.forPrescription(prescr));
    docNoTelematikIDNotListed.performs(
        TaskAbort.asLeistungserbringer(activation.getExpectedResponse()));

    // patient checks auditEvent content
    val auditEvents = patient.performs(DownloadAuditEvent.withQueryParams(getQueryParameters()));
    val prescriptionId = prescr.getExpectedResponse().getTask().getPrescriptionId();
    patient.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLogFor(
                    prescriptionId,
                    "Die Verordnung konnte nicht in die Patientenakte übertragen werden"))
            .isCorrect());
  }
}
