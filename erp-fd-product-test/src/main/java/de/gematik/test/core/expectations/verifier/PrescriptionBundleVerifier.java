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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.KbvProfileRules;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.values.PZN;
import java.util.function.Predicate;
import lombok.val;

public class PrescriptionBundleVerifier {

  private PrescriptionBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxPrescriptionBundle> bundleHasValidAccessCode() {
    Predicate<ErxPrescriptionBundle> predicate =
        bundle -> bundle.getTask().hasAccessCode() && bundle.getTask().getAccessCode().isValid();
    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            ErpAfos.A_19021.getRequirement(),
            format(
                "Der AccessCode muss eine 256 Bit Zufallszahl mit einer Mindestentropie von 120 Bit"
                    + " sein"));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxPrescriptionBundle> bundleContainsAccident(
      AccidentExtension accident) {
    Predicate<ErxPrescriptionBundle> predicate =
        bundle ->
            bundle
                .getKbvBundle()
                .orElseThrow(
                    () -> new MissingFieldException(bundle.getClass(), KbvItaErpStructDef.BUNDLE))
                .getMedicationRequest()
                .getAccident()
                .map(accidentExtension -> accidentExtension.equals(accident))
                .isPresent();

    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            KbvProfileRules.ACCIDENT_EXTENSION,
            format(
                "Das E-Rezept muss ein Unfallkennzeichen mit {0} enthalten",
                accident.accidentCauseType().getDisplay()));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxPrescriptionBundle> bundleContainsPzn(
      PZN expected, RequirementsSet requirementsSet) {

    Predicate<ErxPrescriptionBundle> predicate =
        bundle ->
            bundle
                .getKbvBundle()
                .flatMap(kbvErpBundle -> kbvErpBundle.getMedication().getPznOptional())
                .map(pzn -> pzn.equals(expected))
                .orElse(false);

    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            requirementsSet,
            format("Das E-Rezept muss die Übergebene PZN {0} enthalten", expected.getValue()));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxPrescriptionBundle> bundleContainsNameInMedicationCompound(
      String expected, RequirementsSet requirementsSet) {
    Predicate<ErxPrescriptionBundle> predicate =
        bundle ->
            bundle
                .getKbvBundle()
                .map(
                    kbvErpBundle ->
                        kbvErpBundle
                            .getMedication()
                            .getIngredientFirstRep()
                            .getItemCodeableConcept()
                            .getText())
                .map(expected::equals)
                .orElse(false);

    val step =
        new VerificationStep.StepBuilder<ErxPrescriptionBundle>(
            requirementsSet,
            format("Das E-Rezept muss die Übergebene Medikamentenbezeichnung {0} haben", expected));
    return step.predicate(predicate).accept();
  }
}
