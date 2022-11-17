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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;
import static java.text.MessageFormat.format;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.KbvProfileRules;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.MedicationRequestBuilder;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import de.gematik.test.fuzzing.core.ParameterPair;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept mit Unfallkennzeichen ausstellen")
class ActivateAccidentPrescription extends ErpTest {

  @Actor(name = "Bernd Claudius")
  private DoctorActor bernd;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @TestcaseId("ERP_TASK_ACTIVATE_ACCIDENT_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit Unfallkennzeichen {2} aus")
  @DisplayName("E-Rezept mit validem Unfallkennzeichen ausstellen")
  @MethodSource("validAccidentTypes")
  void activatePrescriptionWithValidAccidentExtension(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      AccidentExtension accident) {

    sina.changeInsuranceType(insuranceType);

    val medication = KbvErpMedicationBuilder.faker().category(MedicationCategory.C_00).build();
    val medicationRequest =
        MedicationRequestBuilder.faker(sina.getPatientData())
            .insurance(sina.getInsuranceCoverage())
            .requester(bernd.getPractitioner())
            .accident(accident)
            .medication(medication)
            .build();

    val kbvBundleBuilder =
        KbvErpBundleBuilder.faker(sina.getKvnr())
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication);

    val activation =
        bernd.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));
    bernd.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_ACCIDENT_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit {2} aus")
  @DisplayName("E-Rezept mit ungültigem Unfallkennzeichen ausstellen")
  @MethodSource("accidentTypesWithManipulators")
  void activatePrescriptionWithInvalidatedAccidentExtension(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<ParameterPair<AccidentExtension, Consumer<KbvErpBundle>>> kbvBundleManipulator) {

    sina.changeInsuranceType(insuranceType);

    val medication = KbvErpMedicationBuilder.faker().category(MedicationCategory.C_00).build();
    val medicationRequest =
        MedicationRequestBuilder.faker(sina.getPatientData())
            .insurance(sina.getInsuranceCoverage())
            .requester(bernd.getPractitioner())
            .accident(kbvBundleManipulator.getParameter().getFirst())
            .medication(medication)
            .build();

    val kbvBundleBuilder =
        KbvErpBundleBuilder.faker(sina.getKvnr())
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication);

    val activation =
        bernd.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withResourceManipulator(kbvBundleManipulator.getParameter().getSecond())
                .withKbvBundleFrom(kbvBundleBuilder));
    bernd.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome()
            .hasResponseWith(returnCodeIsBetween(400, 430, KbvProfileRules.ACCIDENT_EXTENSION))
            .isCorrect());
  }

  static Stream<Arguments> validAccidentTypes() {
    val accidentTypes =
        List.of(
            AccidentExtension.accident(),
            AccidentExtension.accidentAtWork().atWorkplace(),
            AccidentExtension.occupationalDisease());
    return ArgumentComposer.composeWith()
        .arguments(
            VersicherungsArtDeBasis.GKV, // given insurance kind
            PrescriptionAssignmentKind.PHARMACY_ONLY) // expected flow type
        .arguments(VersicherungsArtDeBasis.GKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
        .arguments(VersicherungsArtDeBasis.PKV, PrescriptionAssignmentKind.PHARMACY_ONLY)
        // not yet implemented
        //        .of(
        //            VersicherungsArtDeBasis.PKV,
        //            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
        .multiplyAppend(accidentTypes)
        .create();
  }

  static Stream<Arguments> accidentTypesWithManipulators() {
    val accidentTypes =
        List.of(
            NamedEnvelope.of(
                format("Unfallkennzeichen mit ungültigem Code 3"),
                ParameterPair.of(
                    AccidentExtension.accident(),
                    (Consumer<KbvErpBundle>) b -> {
                      val outerAccidentExtension = getOuterAccidentExtension(b);
                      val unfallKennzeichen = getInnerUnfallkennzeichenExtension(outerAccidentExtension);
                      unfallKennzeichen.getValue().castToCoding(unfallKennzeichen.getValue()).setCode("3");
                    })),
            NamedEnvelope.of(
                format("Unfallkennzeichen 1 ohne Unfalltag"),
                ParameterPair.of(
                    AccidentExtension.accident(),
                    (Consumer<KbvErpBundle>) b -> {
                      val outerAccidentExtension = getOuterAccidentExtension(b);
                      val unfalltagExtension = getInnerUnfalltagExtension(outerAccidentExtension);
                      outerAccidentExtension.getExtension().remove(unfalltagExtension);
                    })),
            NamedEnvelope.of(
                format("Unfallkennzeichen 2 ohne Unfalltag"),
                ParameterPair.of(
                    AccidentExtension.accidentAtWork().atWorkplace(),
                    (Consumer<KbvErpBundle>) b -> {
                      val outerAccidentExtension = getOuterAccidentExtension(b);
                      val unfalltagExtension = getInnerUnfalltagExtension(outerAccidentExtension);
                      outerAccidentExtension.getExtension().remove(unfalltagExtension);
                    })),
            NamedEnvelope.of(
                format("Unfallkennzeichen 2 ohne Unfallbetrieb"),
                ParameterPair.of(
                    AccidentExtension.accidentAtWork().atWorkplace(),
                    (Consumer<KbvErpBundle>) b -> {
                      val outerAccidentExtension = getOuterAccidentExtension(b);
                      val unfallbetriebExtension = getInnerUnfallbetriebExtension(outerAccidentExtension);
                      outerAccidentExtension.getExtension().remove(unfallbetriebExtension);
                    }))
        );

    return ArgumentComposer.composeWith()
        .arguments(
            VersicherungsArtDeBasis.GKV, // given insurance kind
            PrescriptionAssignmentKind.PHARMACY_ONLY) // expected flow type
        .arguments(VersicherungsArtDeBasis.GKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
        .arguments(VersicherungsArtDeBasis.PKV, PrescriptionAssignmentKind.PHARMACY_ONLY)
        // not yet implemented
        //        .of(
        //            VersicherungsArtDeBasis.PKV,
        //            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
        .multiplyAppend(accidentTypes)
        .create();
  }

  private static Extension getOuterAccidentExtension(KbvErpBundle bundle) {
    return bundle.getMedicationRequest().getExtension()
        .stream()
        .filter(ext -> ext.getUrl().equals(KbvItaErpStructDef.ACCIDENT.getCanonicalUrl())
            || ext.getUrl().equals(KbvItaForStructDef.ACCIDENT.getCanonicalUrl()))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(KbvErpMedicationRequest.class, KbvItaForStructDef.ACCIDENT, KbvItaErpStructDef.ACCIDENT));
  }

  private static Extension getInnerUnfallkennzeichenExtension(Extension outer) {
    return getInnerAccidentExtension(outer, "Unfallkennzeichen");
  }

  private static Extension getInnerUnfalltagExtension(Extension outer) {
    return getInnerAccidentExtension(outer, "Unfalltag");
  }

  private static Extension getInnerUnfallbetriebExtension(Extension outer) {
    return getInnerAccidentExtension(outer, "Unfallbetrieb");
  }

  private static Extension getInnerAccidentExtension(Extension outer, String innerUrl) {
    return outer.getExtension().stream().filter(ext -> ext.getUrl().equalsIgnoreCase(innerUrl))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(KbvErpMedicationRequest.class, innerUrl));
  }
}
