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

package de.gematik.test.eml;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.eml.tasks.CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.r4.EpaMedicationRequest;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.profiles.definitions.DgMPStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.MarkdownType;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValuesTest
    extends ErpFhirBuildingTest {

  private KbvErpBundle mockKbvErpBundle;
  private UseTheEpaMockClient mockClient;
  private Actor actor;
  private EpaOpProvidePrescription prescription;
  private VerificationStep<EpaOpProvidePrescription> verifier;
  private KbvErpBundle erpKbvBundle;
  private EpaOpProvidePrescription epaOpProvidePrescription;

  @BeforeEach
  void setup() {
    CoverageReporter.getInstance().startTestcase("testcase");

    mockKbvErpBundle = mock(KbvErpBundle.class);
    mockClient = mock(UseTheEpaMockClient.class);
    actor = mock(Actor.class);
    prescription = mock(EpaOpProvidePrescription.class);
    verifier = mock(VerificationStep.class);

    // Mock prescriptionId
    when(mockKbvErpBundle.getPrescriptionId())
        .thenReturn(PrescriptionId.from("test-prescription-id"));
    // Actor ability
    when(actor.abilityTo(UseTheEpaMockClient.class)).thenReturn(mockClient);
    // Mock prescriptionId
    when(mockKbvErpBundle.getPrescriptionId())
        .thenReturn(PrescriptionId.from("test-prescription-id"));
    // Actor ability
    when(actor.abilityTo(UseTheEpaMockClient.class)).thenReturn(mockClient);

    // --- Zusätzliche Mocks für MedicationRequest und RenderedDosageInstruction ---
    EpaMedicationRequest medicationRequest = mock(EpaMedicationRequest.class);
    when(prescription.getEpaMedicationRequest()).thenReturn(medicationRequest);
    when(medicationRequest.getDosageInstructionDgMPs()).thenReturn(List.of());
    val dosageDgMP =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1)
                    .periodUnit("d")
                    .frequency(1)
                    .when(Timing.EventTiming.MORN)
                    .build())
            .build();

    val medRequest =
        KbvErpMedicationRequestFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withDgmp(dosageDgMP)
            .fake();
    medRequest.addExtension(
        new Extension(
            DgMPStructDef.MR_RENDERED_DOSAGE_INSTRUCTION.getCanonicalUrl(),
            new MarkdownType("1-0-0-0 Stück")));
    erpKbvBundle =
        KbvErpBundleBuilder.builder()
            .version(KbvItaErpVersion.V1_4_0)
            .prescriptionId(PrescriptionId.random())
            .medicationRequest(medRequest)
            .patient(KbvPatientFaker.builder().fake())
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .practitioner(KbvPractitionerFaker.builder().withLanr(LANR.random().getValue()).fake())
            .medicalOrganization(KbvMedicalOrganizationFaker.builder().fake())
            .insurance(KbvCoverageFaker.builder().fake())
            .build();
    epaOpProvidePrescription =
        EpaFhirFactory.create()
            .decode(
                EpaOpProvidePrescription.class,
                ResourceLoader.readFileFromResource(
                    "fhir/valid/parameters/1.3.0/Parameters-example-epa-op-provide-prescription-erp-input-parameters-1.xml"));
  }

  @Test
  void shouldCreateTaskWithFactoryMethod() {
    var task =
        CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues.forPrescription(
            mockKbvErpBundle);
    assertNotNull(task);
  }

  @Test
  void shouldCreateTaskWithAdditionalVerifier() {
    var task =
        CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues.forPrescription(
            mockKbvErpBundle, verifier);
    assertNotNull(task);
  }

  @Test
  void shouldThrowAssertionErrorIfNoPrescriptionFound() {
    when(mockClient.downloadProvidePrescriptionBy(PrescriptionId.from("test-prescription-id")))
        .thenReturn(List.of());

    var task =
        CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues.forPrescription(
            mockKbvErpBundle);

    AssertionError error = assertThrows(AssertionError.class, () -> task.performAs(actor));
    assertTrue(error.getMessage().contains("No EpaOpProvidePrescription found"));
  }

  @Test
  void shouldAssertIfPrescriptionFoundCorrect() {
    when(mockClient.downloadProvidePrescriptionBy(any(PrescriptionId.class)))
        .thenReturn(List.of(epaOpProvidePrescription));

    var task =
        CheckEpaOpProvidePrescriptionsRenderedDosageInstructionValues.forPrescription(erpKbvBundle);

    assertDoesNotThrow(() -> task.performAs(actor));
  }
}
