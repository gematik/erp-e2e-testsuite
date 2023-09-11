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

package de.gematik.test.erezept.integration.task;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.fhir.valuesets.StatusKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.toggle.E2ECucumberTag;
import de.gematik.test.erezept.toggle.FhirCloseSlicingToggle;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import de.gematik.test.fuzzing.kbv.KbvBundleManipulatorFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.thucydides.core.annotations.WithTag;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static java.text.MessageFormat.format;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Invalide Verordnungen")
@Tag("Fuzzing")
@WithTag("Fuzzing")
class ActivateInvalidKbvBundles extends ErpTest {

  private static final Boolean expectClosedSlicing = featureConf.getToggle(new FhirCloseSlicingToggle());

  @Actor(name = "Bernd Claudius")
  private DoctorActor bernd;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  private static ArgumentComposer baseComposer() {
    val composer = ArgumentComposer.composeWith()
        .arguments(
            VersicherungsArtDeBasis.GKV, // given insurance kind
            PrescriptionAssignmentKind.PHARMACY_ONLY) // expected flow type
        .arguments(VersicherungsArtDeBasis.GKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);

    if (cucumberFeatures.isFeatureActive(E2ECucumberTag.INSURANCE_PKV)) {
        composer.arguments(VersicherungsArtDeBasis.PKV, PrescriptionAssignmentKind.PHARMACY_ONLY)
              .arguments(VersicherungsArtDeBasis.PKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);
    }
    return composer;
  }

  private static List<Extension> optionalExtensions() {
    return List.of(
        DmpKennzeichen.DM1.asExtension(),
        // increase number of testdata by adding some other non-fitting extensions
        //            StandardSize.KTP.asExtension(),
        StatusCoPayment.STATUS_2.asExtension(),
        StatusKennzeichen.TSS_SUBSTITUTE.asExtension(),
        new Extension("https://test.erp.gematik.de").setValue(new StringType("Just a Testvalue")));
  }

  static Stream<Arguments> optionalCompositionExtensions() {
    val extensionConsumer =
        optionalExtensions().stream()
            .map(
                ext ->
                    NamedEnvelope.of(
                        format("Composition Extension (''{0}'')", ext.getUrl()),
                        (Consumer<IssuePrescription.Builder>) b -> b.withCompositionExtension(ext)))
            .collect(Collectors.toList());

    return baseComposer().multiplyAppend(extensionConsumer).create();
  }

  static Stream<Arguments> optionalMedicationExtensions() {
    val extensionConsumer =
        optionalExtensions().stream()
            .map(
                ext ->
                    NamedEnvelope.of(
                        format("Medication Extension (''{0}'')", ext.getUrl()),
                        (Consumer<IssuePrescription.Builder>) b -> b.withMedicationExtension(ext)))
            .collect(Collectors.toList());

    return baseComposer().multiplyAppend(extensionConsumer).create();
  }

  static Stream<Arguments> optionalMedicationRequestExtensions() {
    val extensionConsumer =
        optionalExtensions().stream()
            .map(
                ext ->
                    NamedEnvelope.of(
                        format("MedicationRequest Extension (''{0}'')", ext.getUrl()),
                        (Consumer<IssuePrescription.Builder>)
                            b -> b.withMedicationRequestExtension(ext)))
            .collect(Collectors.toList());

    return baseComposer().multiplyAppend(extensionConsumer).create();
  }

  static Stream<Arguments> optionalCoverageExtensions() {
    val extensionConsumer =
        optionalExtensions().stream()
            .map(
                ext ->
                    NamedEnvelope.of(
                        format("Coverage Extension (''{0}'')", ext.getUrl()),
                        (Consumer<IssuePrescription.Builder>) b -> b.withCoverageExtension(ext)))
            .collect(Collectors.toList());

    return baseComposer().multiplyAppend(extensionConsumer).create();
  }

  static Stream<Arguments> optionalPatientExtensions() {
    val extensionConsumer =
        optionalExtensions().stream()
            .map(
                ext ->
                    NamedEnvelope.of(
                        format("Patient Extension (''{0}'')", ext.getUrl()),
                        (Consumer<IssuePrescription.Builder>) b -> b.withPatientExtension(ext)))
            .collect(Collectors.toList());

    return baseComposer().multiplyAppend(extensionConsumer).create();
  }

  static Stream<Arguments> kbvBundleCompositionManipulator() {
    val manipulators = ArgumentComposer.composeWith();
    KbvBundleManipulatorFactory.getCompositionManipulators().forEach(manipulators::arguments);
    return manipulators.multiply(PrescriptionAssignmentKind.class).create();
  }

  static Stream<Arguments> kbvBundleCoverageManipulator() {
    val manipulators = ArgumentComposer.composeWith();
    KbvBundleManipulatorFactory.getCoverageManipulators().forEach(manipulators::arguments);
    return manipulators.multiply(PrescriptionAssignmentKind.class).create();
  }

  static Stream<Arguments> kbvBundleMedicationManipulator() {
    val manipulators = ArgumentComposer.composeWith();
    KbvBundleManipulatorFactory.getMedicationManipulators().forEach(manipulators::arguments);
    return manipulators.multiply(PrescriptionAssignmentKind.class).create();
  }

  static Stream<Arguments> kbvBundleMedicationRequestManipulator() {
    val manipulators = ArgumentComposer.composeWith();
    KbvBundleManipulatorFactory.getMedicationRequestManipulators().forEach(manipulators::arguments);
    return manipulators.multiply(PrescriptionAssignmentKind.class).create();
  }

  static Stream<Arguments> kbvBundleOrganizationManipulator() {
    val manipulators = ArgumentComposer.composeWith();
    KbvBundleManipulatorFactory.getOrganizationManipulators().forEach(manipulators::arguments);
    return manipulators.multiply(PrescriptionAssignmentKind.class).create();
  }

  static Stream<Arguments> kbvBundlePatientManipulator() {
    val manipulators = ArgumentComposer.composeWith();
    KbvBundleManipulatorFactory.getPatientManipulators().forEach(manipulators::arguments);
    return manipulators.multiply(PrescriptionAssignmentKind.class).create();
  }

  static Stream<Arguments> kbvBundlePractitionerManipulator() {
    val manipulators = ArgumentComposer.composeWith();
    KbvBundleManipulatorFactory.getPractitionerManipulators().forEach(manipulators::arguments);
    return manipulators.multiply(PrescriptionAssignmentKind.class).create();
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit optionaler {2} aus")
  @DisplayName(
      "Es dürfen in der Composition nur Extensions verwendet werden, welche explizit nach kbv.ita.erp 1.0.2 gefordert werden")
  @MethodSource("optionalCompositionExtensions")
  void activatePrescriptionWithOptionalCompositionExtensions(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<IssuePrescription.Builder>> extensionProvider) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activatePrescriptionWithOptionalExtensions(insuranceType, assignmentKind, extensionProvider);
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit optionaler {2} aus")
  @DisplayName(
      "Es dürfen in der Medication nur Extensions verwendet werden, welche explizit nach kbv.ita.erp 1.0.2 gefordert werden")
  @MethodSource("optionalMedicationExtensions")
  void activatePrescriptionWithOptionalMedicationExtensions(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<IssuePrescription.Builder>> extensionProvider) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activatePrescriptionWithOptionalExtensions(insuranceType, assignmentKind, extensionProvider);
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit optionaler {2} aus")
  @DisplayName(
      "Es dürfen in der MedicationRequest nur Extensions verwendet werden, welche explizit nach kbv.ita.erp 1.0.2 gefordert werden")
  @MethodSource("optionalMedicationRequestExtensions")
  void activatePrescriptionWithOptionalMedicationRequestExtensions(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<IssuePrescription.Builder>> extensionProvider) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activatePrescriptionWithOptionalExtensions(insuranceType, assignmentKind, extensionProvider);
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit optionaler {2} aus")
  @DisplayName(
      "Es dürfen in der Coverage nur Extensions verwendet werden, welche explizit nach kbv.ita.erp 1.0.2 gefordert werden")
  @MethodSource("optionalCoverageExtensions")
  void activatePrescriptionWithOptionalCoverageExtensions(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<IssuePrescription.Builder>> extensionProvider) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activatePrescriptionWithOptionalExtensions(insuranceType, assignmentKind, extensionProvider);
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_05")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit optionaler {2} aus")
  @DisplayName(
      "Es dürfen in der Patient nur Extensions verwendet werden, welche explizit nach kbv.ita.erp 1.0.2 gefordert werden")
  @MethodSource("optionalPatientExtensions")
  void activatePrescriptionWithOptionalPatientExtensions(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<IssuePrescription.Builder>> extensionProvider) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activatePrescriptionWithOptionalExtensions(insuranceType, assignmentKind, extensionProvider);
  }

  private void activatePrescriptionWithOptionalExtensions(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<IssuePrescription.Builder>> extensionProvider) {

    sina.changePatientInsuranceType(insuranceType);

    val activationBuilder = IssuePrescription.forPatient(sina).ofAssignmentKind(assignmentKind);
    extensionProvider.getParameter().accept(activationBuilder);
    val activation = bernd.performs(activationBuilder.withRandomKbvBundle());

    if (!expectClosedSlicing) {
      // if closed slicing is expected to be deactivated
      bernd.attemptsTo(
          Verify.that(activation)
              .withExpectedType()
              .hasResponseWith(returnCode(202, ErpAfos.A_22927))
              .isCorrect());
    } else {
      // closed slicing should be activated by default: in such cases RC 400 is expected
      bernd.attemptsTo(
          Verify.that(activation)
              .withOperationOutcome(ErpAfos.A_22927)
              .hasResponseWith(returnCode(400, ErpAfos.A_22927))
              .isCorrect());
    }
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_06")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
  @DisplayName("E-Rezept als Verordnender Arzt mit invalider Composition im Verordnungsdatensatz")
  @MethodSource("kbvBundleCompositionManipulator")
  void activatePrescriptionWithInvalidComposition(
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<KbvErpBundle>> kbvBundleManipulator) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activateInvalidPrescription(assignmentKind, kbvBundleManipulator);
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_07")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
  @DisplayName("E-Rezept als Verordnender Arzt mit invalider Coverage im Verordnungsdatensatz")
  @MethodSource("kbvBundleCoverageManipulator")
  void activatePrescriptionWithInvalidCoverage(
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<KbvErpBundle>> kbvBundleManipulator) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activateInvalidPrescription(assignmentKind, kbvBundleManipulator);
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_08")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
  @DisplayName("E-Rezept als Verordnender Arzt mit invalider Medication im Verordnungsdatensatz")
  @MethodSource("kbvBundleMedicationManipulator")
  void activatePrescriptionWithInvalidMedication(
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<KbvErpBundle>> kbvBundleManipulator) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activateInvalidPrescription(assignmentKind, kbvBundleManipulator);
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_09")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
  @DisplayName(
      "E-Rezept als Verordnender Arzt mit invalider MedicationRequest im Verordnungsdatensatz")
  @MethodSource("kbvBundleMedicationRequestManipulator")
  void activatePrescriptionWithInvalidMedicationRequest(
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<KbvErpBundle>> kbvBundleManipulator) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activateInvalidPrescription(assignmentKind, kbvBundleManipulator);
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_10")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
  @DisplayName("E-Rezept als Verordnender Arzt mit invalider Organization im Verordnungsdatensatz")
  @MethodSource("kbvBundleOrganizationManipulator")
  void activatePrescriptionWithInvalidOrganization(
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<KbvErpBundle>> kbvBundleManipulator) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activateInvalidPrescription(assignmentKind, kbvBundleManipulator);
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_11")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
  @DisplayName("E-Rezept als Verordnender Arzt mit invalidem Patient im Verordnungsdatensatz")
  @MethodSource("kbvBundlePatientManipulator")
  void activatePrescriptionWithInvalidPatient(
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<KbvErpBundle>> kbvBundleManipulator) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activateInvalidPrescription(assignmentKind, kbvBundleManipulator);
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_12")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
  @DisplayName("E-Rezept als Verordnender Arzt mit invalidem Practitioner im Verordnungsdatensatz")
  @MethodSource("kbvBundlePractitionerManipulator")
  void activatePrescriptionWithInvalidPractitioner(
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<KbvErpBundle>> kbvBundleManipulator) {

    // this "trick" is required because the serenity report cannot handle that many parameters
    activateInvalidPrescription(assignmentKind, kbvBundleManipulator);
  }

  private void activateInvalidPrescription(
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<KbvErpBundle>> kbvBundleManipulator) {

    val activation =
        bernd.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withResourceManipulator(kbvBundleManipulator.getParameter())
                .withRandomKbvBundle());
    bernd.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400, FhirRequirements.FHIR_PROFILES))
            .isCorrect());
  }
}
