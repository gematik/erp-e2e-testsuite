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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseDiGAFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.r4.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.konnektor.soap.mock.LocalVerifier;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CloseDigaPrescription implements Task {

  private final DequeStrategy order;

  private final Actor receiver;

  public static Builder of(String order) {
    return of(DequeStrategy.fromString(order));
  }

  public static Builder of(DequeStrategy order) {
    return new Builder(order);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val prescriptionPharmacyManager =
        SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val acceptBundle = order.chooseFrom(prescriptionPharmacyManager.getAcceptedPrescriptions());
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val evdgaBundleAsString = LocalVerifier.parse(acceptBundle.getSignedKbvBundle()).getDocument();
    val evdgaBundle = erpClient.decode(KbvEvdgaBundle.class, evdgaBundleAsString);
    val pzn = evdgaBundle.getHealthAppRequest().getPzn();

    // build dispensation
    val md =
        ErxMedicationDispenseDiGAFaker.builder()
            .withKvnr(SafeAbility.getAbility(receiver, ProvideEGK.class).getKvnr())
            .withPrescriptionId(acceptBundle.getTaskId().toPrescriptionId())
            .withPerformer(smcb.getTelematikID())
            .withPzn(pzn.getValue(), evdgaBundle.getHealthAppRequest().getName())
            .fake();
    val operationBuilder = GemOperationInputParameterBuilder.forClosingDiGA();
    val operationParams = operationBuilder.with(md).build();

    // close and dispense
    val cmd =
        new CloseTaskCommand(acceptBundle.getTaskId(), acceptBundle.getSecret(), operationParams);
    val response = erpClient.request(cmd);
    response.getExpectedResource();

    // store dispense Information locally
    val receivedDispensedDrugs = SafeAbility.getAbility(receiver, ReceiveDispensedDrugs.class);
    receivedDispensedDrugs.append(evdgaBundle.getPrescriptionId(), Instant.now());
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    public DequeStrategy order;

    public CloseDigaPrescription to(Actor receiver) {
      return new CloseDigaPrescription(this.order, receiver);
    }
  }
}
