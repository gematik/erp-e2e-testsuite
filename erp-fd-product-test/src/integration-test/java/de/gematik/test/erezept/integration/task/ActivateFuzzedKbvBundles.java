/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;
import static java.text.MessageFormat.format;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.toggle.FuzzingIncrementsToggle;
import de.gematik.test.erezept.toggle.FuzzingIterationsToggle;
import de.gematik.test.fuzzing.core.ByteArrayMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import de.gematik.test.fuzzing.core.StringMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import de.gematik.test.fuzzing.string.SimpleMutatorsFactory;
import de.gematik.test.fuzzing.string.XmlRegExpFactory;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.WithTag;
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
@DisplayName("Verordnungen mit String Fuzzing and SmartFuzzer ")
@Tag("Fuzzing")
@WithTag("Fuzzing")
class ActivateFuzzedKbvBundles extends ErpTest {

  private static final Integer iterations = featureConf.getToggle(new FuzzingIterationsToggle());
  private static final Double percentageIncr = featureConf.getToggle(new FuzzingIncrementsToggle());
  private static final Function<Integer, Double> percentageSupplier =
          input -> ((input + 1) * percentageIncr);

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  public static Stream<Arguments> fuzzingContextProvider() {
      return IntStream.range(0, iterations).mapToObj(x -> {
        float adaptedPercent = (float) (x + 1) / iterations;
        val config = FuzzConfig.getRandom();
        config.setPercentOfAll(config.getPercentOfAll() * adaptedPercent);
        config.setPercentOfEach(config.getPercentOfEach() * adaptedPercent);
        config.setName(config.getName() + "_iter_" + x);
        config.setUsedPercentOfMutators(config.getUsedPercentOfMutators() * adaptedPercent);
        log.info(config.toString());
        return Arguments.arguments(config);
      });
  }

  @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_01")
  @ParameterizedTest(
          name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
  @DisplayName("Mit String Fuzzing invalidierte Verordnungen")
  @MethodSource("kbvBundleStringFuzzer")
  void activatePrescriptionWithStringFuzzing(NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_02")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName("Mit String Fuzzing invalidierte Composition-Resource innerhalb der Verordnungen")
    @MethodSource("kbvBundleCompositionStringFuzzer")
  void activatePrescriptionWithStringFuzzingComposition(NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_03")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName("Mit String Fuzzing invalidierte Patient-Resource innerhalb der Verordnungen")
    @MethodSource("kbvBundlePatientStringFuzzer")
  void activatePrescriptionWithStringFuzzingPatient(NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_04")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName("Mit String Fuzzing invalidierte Medication-Resource innerhalb der Verordnungen")
    @MethodSource("kbvBundleMedicationStringFuzzer")
  void activatePrescriptionWithStringFuzzingMedication(NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_05")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName(
            "Mit String Fuzzing invalidierte MedicationRequest-Resource innerhalb der Verordnungen")
    @MethodSource("kbvBundleMedicationRequestStringFuzzer")
  void activatePrescriptionWithStringFuzzingMedicationRequest(
          NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_06")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName("Mit String Fuzzing invalidierte Practitioner-Resource innerhalb der Verordnungen")
    @MethodSource("kbvBundlePractitionerStringFuzzer")
  void activatePrescriptionWithStringFuzzingPractitioner(
          NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_07")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName("Mit String Fuzzing invalidierte Organization-Resource innerhalb der Verordnungen")
    @MethodSource("kbvBundleOrganizationStringFuzzer")
  void activatePrescriptionWithStringFuzzingOrganization(
          NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_08")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName("Mit String Fuzzing invalidierte Coverage-Resource innerhalb der Verordnungen")
    @MethodSource("kbvBundleCoverageStringFuzzer")
  void activatePrescriptionWithStringFuzzingCoverage(NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_09")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName("Mit String Fuzzing invalidierte Meta-Informationen innerhalb der Verordnungen")
    @MethodSource("kbvBundleMetaStringFuzzer")
  void activatePrescriptionWithStringFuzzingMeta(NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_10")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName("Mit String Fuzzing invalidierter Identifier innerhalb der Verordnungen")
    @MethodSource("kbvBundleIdentifierStringFuzzer")
  void activatePrescriptionWithStringFuzzingIdentifier(NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_11")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName("Mit String Fuzzing invalidierter Codings innerhalb der Verordnungen")
    @MethodSource("kbvBundleCodingStringFuzzer")
  void activatePrescriptionWithStringFuzzingCodings(NamedEnvelope<StringMutator> fuzzingStep) {
    executeTest(b -> b.withStringFuzzing(fuzzingStep.getParameter()));
  }

    @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_12")
    @ParameterizedTest(
            name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept mit ''{1}'' aus")
    @DisplayName("Mit ByteArray Fuzzing der signierten Verordnung Verordnungen")
    @MethodSource("kbvBundleByteArrayFuzzer")
    void activatePrescriptionWithStringFuzzingSignedBundle(
            NamedEnvelope<ByteArrayMutator> fuzzingStep) {
      executeTest(b -> b.withByteArrayFuzzing(fuzzingStep.getParameter()));
    }

  @TestcaseId("ERP_TASK_ACTIVATE_FUZZING_13")
  @DisplayName("FhirFuzz hat mit Random gefuzzten Werten versucht Rezepte einzustellen")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein smartFuzzed E-Rezept mit ''{0}'' aus")
  @MethodSource("fuzzingContextProvider")
  void activatePrescriptionWithSmartFuzzer(FuzzConfig fuzzConfig) {
    var insuranceType =
        GemFaker.randomElement(VersicherungsArtDeBasis.PKV, VersicherungsArtDeBasis.GKV);
    var assignmentKind =
        GemFaker.randomElement(
            PrescriptionAssignmentKind.PHARMACY_ONLY, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);

    sina.changePatientInsuranceType(insuranceType);
    val fuzzerContext = new FuzzerContext(fuzzConfig);
    val fhirFuzzer = new FhirFuzzImpl(fuzzerContext);
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withSmartFuzzer(fhirFuzzer)
                .withRandomKbvBundle());
    val fuzzLog = fuzzerContext.getOperationLogs().stream().map(Object::toString).collect(Collectors.joining("\n"));
    log.info("FuzzLogWithSmartFuzzer:\n" + fuzzLog);

    doctor.attemptsTo(
            Verify.that(activation).withIndefiniteType()
                    .hasResponseWith(returnCodeIsBetween(400, 499, FhirRequirements.FHIR_XML_PARSING))
                    .isCorrect());
  }

  private void executeTest(Consumer<IssuePrescription.Builder> mutator) {
    val issueManipulatedPrescription = IssuePrescription.forPatient(sina);
    mutator.accept(issueManipulatedPrescription);

    val activation = doctor.performs(issueManipulatedPrescription.withRandomKbvBundle());

    doctor.attemptsTo(
            Verify.that(activation)
                    .withIndefiniteType()
                    .hasResponseWith(returnCodeIsBetween(203, 499, FhirRequirements.FHIR_XML_PARSING))
                    .isCorrect());
  }

  static Stream<Arguments> kbvBundleStringFuzzer() {
    return fuzzingComposer("Bundle").create();
  }

  static Stream<Arguments> kbvBundleCompositionStringFuzzer() {
    return fuzzingComposer("Composition").create();
  }

  static Stream<Arguments> kbvBundlePatientStringFuzzer() {
    return fuzzingComposer("Patient").create();
  }

  static Stream<Arguments> kbvBundleMedicationStringFuzzer() {
    return fuzzingComposer("Medication").create();
  }

  static Stream<Arguments> kbvBundleMedicationRequestStringFuzzer() {
    return fuzzingComposer("MedicationRequest").create();
  }

  static Stream<Arguments> kbvBundlePractitionerStringFuzzer() {
    return fuzzingComposer("Practitioner").create();
  }

  static Stream<Arguments> kbvBundleOrganizationStringFuzzer() {
    return fuzzingComposer("Organization").create();
  }

  static Stream<Arguments> kbvBundleCoverageStringFuzzer() {
    return fuzzingComposer("Coverage").create();
  }

  static Stream<Arguments> kbvBundleMetaStringFuzzer() {
    return fuzzingComposer("meta").create();
  }

  static Stream<Arguments> kbvBundleIdentifierStringFuzzer() {
    return fuzzingComposer("identifier").create();
  }

  static Stream<Arguments> kbvBundleCodingStringFuzzer() {
    return fuzzingComposer("coding").create();
  }

  private static ArgumentComposer fuzzingComposer(String betweenTag) {
    val mutators = ArgumentComposer.composeWith();

    IntStream.range(0, iterations)
        .forEach(
            idx -> {
              mutators.arguments(
                  NamedEnvelope.of(
                      format(
                          "Fuzzing über den {0}-Tag im Verordnungsdatensatz mit {1} %",
                          betweenTag, percentageSupplier.apply(idx)),
                      SimpleMutatorsFactory.everything(
                          XmlRegExpFactory.betweenXmlTag(betweenTag),
                          percentageSupplier.apply(idx))));
            });

    return mutators;
  }

  static Stream<Arguments> kbvBundleByteArrayFuzzer() {
    val mutators = ArgumentComposer.composeWith();

    IntStream.range(0, iterations)
        .forEach(
            idx -> {
              mutators.arguments(
                  NamedEnvelope.of(
                      format(
                          "Fuzzing des signierten Verordnungsdatensatz mit {0} %",
                          percentageSupplier.apply(idx)),
                      SimpleMutatorsFactory.wholeByteArray(percentageSupplier.apply(idx))));
            });

    return mutators.create();
  }


}
