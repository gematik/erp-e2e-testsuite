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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.exceptions.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.pspwsclient.dataobjects.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.nio.charset.*;
import lombok.*;
import net.serenitybdd.screenplay.*;

public class DecryptPSPMessage implements Task {
  public static DecryptPSPMessage receivedFromPharmacyService() {
    return new DecryptPSPMessage();
  }

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
    val prescriptionInfo =
        PharmacyPrescriptionInformation.fromRawString(
            new String(decryptedRecipe.getPayload(), StandardCharsets.UTF_8));
    val dmc =
        DmcPrescription.ownerDmc(
            prescriptionInfo.getTaskID(), new AccessCode(prescriptionInfo.getAccessCode()));
    prescriptionStack.appendAssignedPrescription(dmc);
  }
}
