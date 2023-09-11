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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhirdump.FhirDumper;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.ChargeItemChangeAuthorization;
import de.gematik.test.erezept.screenplay.util.DataMatrixCodeGenerator;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HandoverChargeItemAuthorization implements Task {

  private final DequeStrategy deque;
  private final Actor pharmacy;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val dispensedDrugsStack = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val authorizationStack = SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class);

    val dispensedPrescriptionId = deque.chooseFrom(dispensedDrugsStack.getDispensedDrugsList());
    val cmd = new ChargeItemGetByIdCommand(dispensedPrescriptionId);
    val chargeItemBundle = erpClient.request(cmd).getExpectedResource();
    val chargeItem = chargeItemBundle.getChargeItem();
    val cica =
        ChargeItemChangeAuthorization.forChargeItem(
            chargeItem,
            chargeItem
                .getAccessCode()
                .orElseThrow(() -> new MissingFieldException(ErxChargeItem.class, "AccessCode")));
    writeDmcToReport(cica);
    authorizationStack.getChargeItemChangeAuthorizations().append(cica);
  }

  @SneakyThrows
  private void writeDmcToReport(ChargeItemChangeAuthorization dmc) {
    // write the DMC to file and append to the Serenity Report
    val dmcPath =
        Path.of(
            "target",
            "site",
            "serenity",
            "dmcs",
            format("charge_item_auth_{0}.png", dmc.getPrescriptionId().getValue()));
    val bitMatrix =
        DataMatrixCodeGenerator.generateDmc(
            dmc.getPrescriptionId().getValue(), dmc.getAccessCode());
    DataMatrixCodeGenerator.writeToFile(bitMatrix, dmcPath.toFile());

    Serenity.recordReportData()
        .withTitle(
            format(
                "ChargeItem Change Data Matrix Code for {0} with {1}",
                dmc.getPrescriptionId().getValue(), dmc.getAccessCode().getValue()))
        .downloadable()
        .fromFile(dmcPath);

    FhirDumper.getInstance()
        .writeDump(
            format(
                "DMC for {0} with AccessCode {1}",
                dmc.getPrescriptionId().getValue(), dmc.getAccessCode().getValue()),
            format("charge_item_auth_{0}.png", dmc.getPrescriptionId().getValue()),
            file -> DataMatrixCodeGenerator.writeToFile(bitMatrix, file));
  }

  public static Builder forChargeItem(String order) {
    return forChargeItem(DequeStrategy.fromString(order));
  }

  public static Builder forChargeItem(DequeStrategy deque) {
    return new Builder(deque);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy deque;

    public HandoverChargeItemAuthorization to(Actor actor) {
      return toThePharmacy(actor);
    }

    public HandoverChargeItemAuthorization toThePharmacy(Actor actor) {
      return new HandoverChargeItemAuthorization(deque, actor);
    }
  }
}
