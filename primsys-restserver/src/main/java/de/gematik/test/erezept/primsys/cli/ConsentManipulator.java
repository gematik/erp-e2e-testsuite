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

package de.gematik.test.erezept.primsys.cli;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.usecases.ConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.ConsentGetCommand;
import de.gematik.test.erezept.client.usecases.ConsentPostCommand;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.exceptions.ConfigurationMappingException;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardFactory;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import picocli.CommandLine;

@Slf4j
public class ConsentManipulator {

  @CommandLine.Option(
      names = "--conf",
      paramLabel = "CONFIG",
      type = Path.class,
      required = true,
      description = "Path to a Configuration File")
  private Path config;

  @CommandLine.Option(
      names = "--env",
      paramLabel = "ENV",
      type = String.class,
      required = true,
      description = "Environment to delete prescriptions from")
  private String environment;

  @CommandLine.Option(
      names = "--kvnr",
      paramLabel = "KVNR",
      type = String.class,
      description = "KVNR to post the Consent for")
  private String kvnr;

  @CommandLine.Command(name = "get")
  public Integer getConsent() {
    val returnCode = 0;
    val erpClient = this.createErpClient();
    val response = erpClient.request(new ConsentGetCommand());
    log.info(format("Response for GET Consent {0}: {1}", kvnr, response));
    if (response.isOperationOutcome()) {
      this.logOperationOutcome(response.getAsOperationOutcome());
    } else {
      val consent = erpClient.getFhir().encode(response.getExpectedResource(), EncodingType.JSON, true);
      System.out.println(consent);
    }
    return returnCode;
  }
  
  @CommandLine.Command(name = "create")
  public Integer createConsent() {
    val returnCode = 0;

    val erpClient = this.createErpClient();
    val response = erpClient.request(new ConsentPostCommand(KVNR.from(kvnr)));
    log.info(format("Response for POST Consent {0}: {1}", kvnr, response));
    if (response.isOperationOutcome()) {
      this.logOperationOutcome(response.getAsOperationOutcome());
    } else {
      val consent = erpClient.getFhir().encode(response.getExpectedResource(), EncodingType.JSON, true);
      System.out.println(consent);
    }

    return returnCode;
  }

  @CommandLine.Command(name = "delete")
  public Integer deleteConsent() {
    val returnCode = 0;
    val erpClient = this.createErpClient();
    val response = erpClient.request(new ConsentDeleteCommand());
    log.info(format("Response for DELETE Consent {0}: {1}", kvnr, response));
    if (response.isOperationOutcome()) {
      this.logOperationOutcome(response.getAsOperationOutcome());
    } else {
      log.info("Consent deleted successfully");
    }
    return returnCode;
  }
  
  private ErpClient createErpClient() {
    val configFile = config.toFile();
    log.info("Initialize Erp-Client with Config from " + configFile.getAbsolutePath());
    val cfg = TestsuiteConfiguration.getInstance(configFile);
    val smartcards = SmartcardFactory.getArchive();

    val env =
            cfg.getEnvironments().stream()
                    .filter(envCfg -> envCfg.getName().equalsIgnoreCase(environment))
                    .findFirst()
                    .orElseThrow(
                            () ->
                                    new ConfigurationMappingException(
                                            environment,
                                            cfg.getEnvironments().stream()
                                                    .map(EnvironmentConfiguration::getName)
                                                    .toList()));

    val egk = smartcards.getEgkByKvnr(kvnr);
    val patientConfig =
            cfg.getActors().getPatients().stream()
                    .filter(patient -> patient.getEgkIccsn().equals(egk.getIccsn()))
                    .findFirst()
                    .orElseThrow(
                            () ->
                                    new ConfigurationMappingException(
                                            kvnr, smartcards.getEgkCards().stream().map(Egk::getKvnr).toList()));

    val erpClient = ErpClientFactory.createErpClient(env, patientConfig);
    erpClient.authenticateWith(egk);
    return erpClient;
  }
  
  private void logOperationOutcome(OperationOutcome oo) {
    log.info(format("OperationOutcome: {0}", oo.getIssueFirstRep().getDetails().getText()));
  }
}
