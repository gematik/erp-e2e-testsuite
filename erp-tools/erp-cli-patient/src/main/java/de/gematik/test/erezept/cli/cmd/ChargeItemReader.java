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

package de.gematik.test.erezept.cli.cmd;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.client.usecases.ChargeItemGetCommand;
import de.gematik.test.erezept.fhir.r4.dav.DavInvoice;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "chargeitems",
    description = "show charge items",
    mixinStandardHelpOptions = true)
public class ChargeItemReader extends BaseRemoteCommand {

  @Override
  public void performFor(Egk egk, ErpClient erpClient) {
    log.info(
        format("Show charge items for {0} from {1}", egk.getKvnr(), this.getEnvironmentName()));

    val cmd = new ChargeItemGetCommand();
    val response = erpClient.request(cmd);
    val chargeItemSet = response.getExpectedResource();
    chargeItemSet.getChargeItems().forEach(ci -> printChargeItem(erpClient, ci));
  }

  private void printChargeItem(ErpClient client, ErxChargeItem shallowChargeItem) {
    val cmd = new ChargeItemGetByIdCommand(shallowChargeItem.getPrescriptionId());
    val response = client.request(cmd);
    val chargeItemBundle = response.getExpectedResource();
    val chargeItem = chargeItemBundle.getChargeItem();
    val davBundle = chargeItemBundle.getAbgabedatenBundle();
    System.out.println(
        format(
            "=> {0} ChargeItem für Verordnung {1}",
            chargeItem.getStatus(), chargeItem.getPrescriptionId().getValue()));
    System.out.println(
        format(
            "\tAccessCode:  {0}",
            chargeItem.getAccessCode().map(SemanticValue::getValue).orElse("n/a")));
    System.out.println(
        format("\tErstellt am: {0}", chargeItem.getEnteredDateElement().asStringValue()));
    System.out.println(
        format(
            "\tErsteller:      {0} für {1}",
            chargeItem.getEntererTelematikId(), chargeItem.getSubjectKvnr().getValue()));
    printInvoice(davBundle.getInvoice());
    System.out.println("-------------");
  }

  private void printInvoice(DavInvoice invoice) {
    System.out.println(format("=> Rechnung {0} für PZN {1}", invoice.getId(), invoice.getPzn()));
    System.out.println(
        format(
            "\tGesamtpreis: {0} {1} (MwSt {2})",
            invoice.getTotalPrice(), invoice.getCurrency(), invoice.getVAT()));
    System.out.println(
        format("\tZuzahlung:   {0} {1}", invoice.getTotalCoPayment(), invoice.getCurrency()));
    invoice
        .getPriceComponents()
        .forEach(
            pc ->
                System.out.println(
                    format(
                        "\t\t- {0} {1} {2}",
                        pc.getType(), pc.getAmount().getValue(), pc.getAmount().getCurrency())));
  }
}
