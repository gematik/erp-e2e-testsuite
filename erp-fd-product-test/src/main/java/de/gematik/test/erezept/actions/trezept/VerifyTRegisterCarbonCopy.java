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

package de.gematik.test.erezept.actions.trezept;

import static de.gematik.test.core.expectations.verifier.tprescriptionverifier.CarbonCopyVerifier.*;

import de.gematik.test.core.expectations.verifier.tprescriptionverifier.CarbonCopyVerifier;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.erp.tprescription.ErpTPrescriptionCarbonCopy;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.trezept.TRegisterLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class VerifyTRegisterCarbonCopy implements Performable {

  private final List<TRegisterLog> logs;
  private final PrescriptionId prescriptionId;
  private final ErxMedicationDispenseBundle medDispenseBundle;
  private Boolean shouldCheckAuthHeadder = false;

  private VerifyTRegisterCarbonCopy(
      List<TRegisterLog> logs,
      PrescriptionId prescriptionId,
      @Nullable ErxMedicationDispenseBundle medDispenseBundle,
      @Nullable Boolean checkAuthHeadder) {
    this.logs = logs;
    this.prescriptionId = prescriptionId;
    this.medDispenseBundle = medDispenseBundle;
    this.shouldCheckAuthHeadder = checkAuthHeadder;
  }

  public VerifyTRegisterCarbonCopy checkAccessToken() {
    CarbonCopyVerifier.checkPresenceAccessToken().getPredicate().test(logs);
    return this;
  }

  public static VerifyTRegisterCarbonCopy from(
      List<TRegisterLog> logs,
      PrescriptionId prescriptionId,
      ErxMedicationDispenseBundle medDisp,
      @Nullable Boolean shouldCheckAuthHeadder) {
    return new VerifyTRegisterCarbonCopy(logs, prescriptionId, medDisp, shouldCheckAuthHeadder);
  }

  @Override
  @Step(
      "{0} verifiziert die Korrektheit des digitalen E-Rezept-Durchschlags vom Bfarm Mock zur"
          + " Prescription #prescriptionId und den logs #logs ")
  public void performAs(Actor actor) {
    if (logs == null || logs.isEmpty()) {
      throw new AssertionError("No carbon copy found in T-Register");
    }
    val httpRequest = logs.get(0).request();
    val body = httpRequest.bodyAsString();

    Optional.ofNullable(medDispenseBundle)
        .ifPresent(
            medDispBundle -> {
              val carbCopy =
                  actor
                      .abilityTo(UseTheErpClient.class)
                      .getFhir()
                      .decode(ErpTPrescriptionCarbonCopy.class, body);

              List<Pair<ErxMedicationDispense, GemErpMedication>> medDisp =
                  medDispenseBundle.getDispensePairBy(prescriptionId);

              val verifiers =
                  new ArrayList<>(
                      List.of(
                          checkPznFromGemMedication(medDisp),
                          checkMedicationName(medDisp),
                          checkPrescriptionId(medDisp),
                          checkDarreichungsformInPrescription(medDisp),
                          checkDarreichungsformInDispensation(medDisp)));
              verifiers.forEach(v -> v.apply(carbCopy));
            });
    if (Boolean.TRUE.equals(shouldCheckAuthHeadder)) {
      checkAccessToken();
    }
  }
}
