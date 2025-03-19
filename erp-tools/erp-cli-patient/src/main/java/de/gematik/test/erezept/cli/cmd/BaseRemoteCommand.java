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
 */

package de.gematik.test.erezept.cli.cmd;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.cli.cfg.ConfigurationFactory;
import de.gematik.test.erezept.cli.param.EgkParameter;
import de.gematik.test.erezept.cli.param.EnvironmentParameter;
import de.gematik.test.erezept.cli.printer.ResourcePrinter;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import java.util.concurrent.Callable;
import lombok.val;
import picocli.CommandLine;

public abstract class BaseRemoteCommand implements Callable<Integer> {

  @CommandLine.Mixin private EgkParameter egkParameter;

  @CommandLine.Mixin private EnvironmentParameter environmentParameter;

  protected final ResourcePrinter resourcePrinter = new ResourcePrinter();

  @Override
  public final Integer call() {
    val sca = SmartcardArchive.fromResources();
    val egks = egkParameter.getEgks(sca);
    val env = this.getEnvironment();

    egks.forEach(
        egk -> {
          val patientConfig = ConfigurationFactory.createPatientConfigurationFor(egk);
          val erpClient = ErpClientFactory.createErpClient(env, patientConfig);
          erpClient.authenticateWith(egk);
          this.performFor(egk, erpClient);
        });
    return 0;
  }

  public final EnvironmentConfiguration getEnvironment() {
    return environmentParameter.getEnvironment();
  }

  public final String getEnvironmentName() {
    return this.getEnvironment().getName();
  }

  public abstract void performFor(Egk egk, ErpClient erpClient);
}
