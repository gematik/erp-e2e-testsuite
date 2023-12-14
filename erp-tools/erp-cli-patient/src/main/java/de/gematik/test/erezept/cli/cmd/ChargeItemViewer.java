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

package de.gematik.test.erezept.cli.cmd;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.cli.cfg.ConfigurationFactory;
import de.gematik.test.erezept.cli.param.EgkParameter;
import de.gematik.test.erezept.cli.param.EnvironmentParameter;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.client.usecases.ChargeItemGetCommand;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.resources.dav.DavInvoice;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.values.Value;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardFactory;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "chargeitems",
    description = "show charge items",
    mixinStandardHelpOptions = true)
public class ChargeItemViewer implements Callable<Integer> {

  @CommandLine.Mixin private EgkParameter egkParameter;

  @CommandLine.Mixin private EnvironmentParameter environmentParameter;

  @Override
  public Integer call() throws Exception {
    val sca = SmartcardFactory.getArchive();
    val egks = egkParameter.getEgks(sca);
    val env = environmentParameter.getEnvironment();

    egks.forEach(egk -> this.performFor(env, egk));
    return 0;
  }

  private void performFor(EnvironmentConfiguration env, Egk egk) {
    val patientConfig = ConfigurationFactory.createPatientConfigurationFor(egk);
    val erpClient = ErpClientFactory.createErpClient(env, patientConfig);
    erpClient.authenticateWith(egk);
    log.info(format("Show charge items for {0} from {1}", egk.getKvnr(), env.getName()));

    val cmd = new ChargeItemGetCommand();
    val response = erpClient.request(cmd);
    val chargeItemSet = response.getExpectedResource();
    chargeItemSet.getChargeItems().forEach(ci -> printChargeItem(erpClient, ci));
  }

  private void printChargeItem(ErpClient client, ErxChargeItem shallowChargeItem) {
    val cmd = new ChargeItemGetByIdCommand(shallowChargeItem.getPrescriptionId());
    val chargeItemBundle = client.request(cmd).getExpectedResource();
    val chargeItem = chargeItemBundle.getChargeItem();
    val davBundle = chargeItemBundle.getAbgabedatenBundle();
    System.out.println(
        format(
            "=> {0} ChargeItem für Verordnung {1}",
            chargeItem.getStatus(), chargeItem.getPrescriptionId().getValue()));
    System.out.println(
        format("\tAccessCode:  {0}", chargeItem.getAccessCode().map(Value::getValue).orElse("n/a")));
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
    System.out.println(
        format("=> Rechnung {0} für PZN {1}", invoice.getId(), invoice.getPzn()));
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
