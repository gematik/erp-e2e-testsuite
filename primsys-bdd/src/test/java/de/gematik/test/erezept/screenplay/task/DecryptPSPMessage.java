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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.pspwsclient.dataobjects.PharmacyPrescriptionInformation;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UsePspClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class DecryptPSPMessage implements Task {
  @SneakyThrows
  @Override
  public <T extends Actor> void performAs(T actor) {
    val pspClient = SafeAbility.getAbility(actor, UsePspClient.class);
    val konectorClient = SafeAbility.getAbility(actor, UseTheKonnektor.class);
    val prescriptionStack = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val pspMessage = pspClient.consumeOldest(500);

    val encrypedReciepData =
        pspMessage
            .orElseThrow(
                () ->
                    new MissingPreconditionError(
                        format(
                            "Der Akteur {0} hat vom PSP-Server keine Nachricht erhalten",
                            actor.getName())))
            .getBlob();
    val decryptedRecipe = konectorClient.decrypt(encrypedReciepData);
    val prescriptionInfo = PharmacyPrescriptionInformation.fromRawString(decryptedRecipe);
    val dmc =
        DmcPrescription.ownerDmc(
            prescriptionInfo.getTaskID(), new AccessCode(prescriptionInfo.getAccessCode()));
    prescriptionStack.appendAssignedPrescription(dmc);
  }

  public static DecryptPSPMessage receivedFromPharmacyService() {
    return new DecryptPSPMessage();
  }
}
