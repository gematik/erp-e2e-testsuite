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

package de.gematik.test.erezept.integration.bundlevalidation;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;
import static de.gematik.test.fuzzing.kbv.KbvBundleManipulatorFactory.*;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.ReferenceFeatureToggle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.toggle.RefenreceValidationActive;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
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
@DisplayName("E-Rezept Verordnungsbundle mit verschiedenen Referenzen")
@Tag("ReferenceValidation")
class ValidateBundleReferencesIT extends ErpTest {
  private static final Boolean REFERENCE_VALIDATION_IS_ACTIVE =
      featureConf.getToggle(new RefenreceValidationActive());

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  private static Stream<Arguments> getOidBundleManipulationComposer() {
    return ArgumentComposer.composeWith()
        .arguments()
        .multiply(0, getReferenceToOidReferenceManipulators())
        .create();
  }

  private static Stream<Arguments> getResourceIdManipulators() {
    return ArgumentComposer.composeWith()
        .arguments()
        .multiply(0, getResourceIdReduceManipulators())
        .create();
  }

  private static Stream<Arguments> getResourceIdAndFullUrlDifferators() {
    return ArgumentComposer.composeWith()
        .arguments()
        .multiply(0, getResourceIdAndFullUrlDiffManipulators())
        .create();
  }

  private static Stream<Arguments> getCompositionReferenceManipulator() {
    return ArgumentComposer.composeWith()
        .arguments()
        .multiply(0, getCompositionReferencedManipulators())
        .create();
  }

  @TestcaseId("ERP_BUNDLE_REFERENCE_VALIDATION_1")
  @ParameterizedTest(
      name = "[{index}] -> Ein Arzt versucht einen Task zu aktivieren aber gibt {0} an.")
  @DisplayName(
      "Es muss geprüft werden, dass eine Verordnung die eine OID in der FullUrl trägt nicht vom FD"
          + " akzeptiert wird.")
  @MethodSource("getOidBundleManipulationComposer")
  void backendShouldNotAcceptNonEqualIdInBundleAndFullUrl(
      NamedEnvelope<FuzzingMutator<KbvErpBundle>> manipulator) {
    System.setProperty(
        ReferenceFeatureToggle.TOGGLE_KEY, ReferenceFeatureToggle.RefencingType.UUID.getValue());
    val response =
        doctor.performs(
            IssuePrescription.forPatient(patient)
                .withResourceManipulator(manipulator.getParameter())
                .withRandomKbvBundle());
    if (REFERENCE_VALIDATION_IS_ACTIVE) {
      doctor.attemptsTo(
          Verify.that(response)
              .withOperationOutcome()
              .hasResponseWith(returnCode(400))
              .and(
                  operationOutcomeContainsInDetailText(
                      "Format der fullUrl ist ungültig", ErpAfos.A_26233))
              .isCorrect());
    } else {
      doctor.attemptsTo(
          Verify.that(response).withExpectedType().hasResponseWith(returnCode(200)).isCorrect());
    }
  }

  @TestcaseId("ERP_BUNDLE_REFERENCE_VALIDATION_2")
  @ParameterizedTest(
      name = "[{index}] -> Ein Arzt versucht ein PrescriptionBundle einzustellen und {0}")
  @DisplayName(
      "Es muss geprüft werden, dass beim einstellen einer Verordnung die Ressource Id nicht fehlt")
  @MethodSource("getResourceIdManipulators")
  void backendShouldNotAcceptBundleResourcesWithoutId(
      NamedEnvelope<FuzzingMutator<KbvErpBundle>> manipulator) {
    System.setProperty(
        ReferenceFeatureToggle.TOGGLE_KEY, ReferenceFeatureToggle.RefencingType.UUID.getValue());
    val response =
        doctor.performs(
            IssuePrescription.forPatient(patient)
                .withResourceManipulator(manipulator.getParameter())
                .withRandomKbvBundle());
    if (REFERENCE_VALIDATION_IS_ACTIVE) {
      doctor.attemptsTo(
          Verify.that(response)
              .withOperationOutcome()
              .hasResponseWith(returnCode(400))
              .and(
                  operationOutcomeContainsInDetailText(
                      "Die ID einer Ressource im Bundle ist nicht vorhanden", ErpAfos.A_27648))
              .isCorrect());
    } else {
      doctor.attemptsTo(
          Verify.that(response)
              .withOperationOutcome()
              .hasResponseWith(returnCode(400))
              .isCorrect());
    }
  }

  @TestcaseId("ERP_BUNDLE_REFERENCE_VALIDATION_3")
  @ParameterizedTest(
      name = "[{index}] -> Ein Arzt versucht ein PrescriptionBundle einzustellen und setzt {0}")
  @DisplayName(
      "Es muss geprüft werden, dass beim einstellen einer Verordnung die Ressource Id und die"
          + " fullUrl übereinstimmen")
  @MethodSource("getResourceIdAndFullUrlDifferators")
  void backendShouldNotAcceptBundleEntriesWithDiffsInIdAndFullUrl(
      NamedEnvelope<FuzzingMutator<KbvErpBundle>> manipulator) {
    System.setProperty(
        ReferenceFeatureToggle.TOGGLE_KEY, ReferenceFeatureToggle.RefencingType.HTTP.getValue());
    val response =
        doctor.performs(
            IssuePrescription.forPatient(patient)
                .withResourceManipulator(manipulator.getParameter())
                .withRandomKbvBundle());

    if (REFERENCE_VALIDATION_IS_ACTIVE) {
      doctor.attemptsTo(
          Verify.that(response)
              .withOperationOutcome()
              .hasResponseWith(returnCode(400))
              .and(
                  operationOutcomeContainsInDetailText(
                      "Die ID einer Ressource und die ID der zugehörigen fullUrl stimmen nicht"
                          + " überein.",
                      ErpAfos.A_26229))
              .isCorrect());
    } else {
      doctor.attemptsTo(
          Verify.that(response).withExpectedType().hasResponseWith(returnCode(200)).isCorrect());
    }
  }

  @TestcaseId("ERP_BUNDLE_REFERENCE_VALIDATION_4")
  @ParameterizedTest(
      name = "[{index}] -> Ein Arzt versucht ein PrescriptionBundle einzustellen und setzt die {0}")
  @DisplayName(
      "Es muss geprüft werden, dass beim einstellen einer Verordnung die Referenzen nicht auflösbar"
          + " (vorhanden) sind")
  @MethodSource("getCompositionReferenceManipulator")
  void backendShouldNotAcceptRelativeReferences(
      NamedEnvelope<FuzzingMutator<KbvErpBundle>> manipulator) {
    System.setProperty(
        ReferenceFeatureToggle.TOGGLE_KEY, ReferenceFeatureToggle.RefencingType.UUID.getValue());
    val response =
        doctor.performs(
            IssuePrescription.forPatient(patient)
                .withResourceManipulator(manipulator.getParameter())
                .withRandomKbvBundle());
    if (REFERENCE_VALIDATION_IS_ACTIVE) {
      doctor.attemptsTo(
          Verify.that(response)
              .withOperationOutcome()
              .hasResponseWith(returnCode(400))
              .and(
                  operationOutcomeContainsInDetailText(
                      "Referenz einer Ressource konnte nicht aufgelöst werden.", ErpAfos.A_27649))
              .isCorrect());
    } else {
      doctor.attemptsTo(
          Verify.that(response)
              .withOperationOutcome()
              .hasResponseWith(returnCode(400))
              .isCorrect());
    }
  }
}
