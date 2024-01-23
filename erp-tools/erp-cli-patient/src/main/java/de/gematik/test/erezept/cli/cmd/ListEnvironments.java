/*
 * Copyright 2023 gematik GmbH
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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.cli.cfg.ErpEnvironmentsConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "lsenv",
    description = "list available TI environments from configuration",
    mixinStandardHelpOptions = true)
public class ListEnvironments implements Callable<Integer> {
  @Override
  public Integer call() throws Exception {
    val cfg = new ErpEnvironmentsConfiguration();
    cfg.getEnvironments().forEach(this::printInformation);
    return 0;
  }

  private void printInformation(EnvironmentConfiguration env) {
    System.out.println(format("Environment: {0}", env.getName()));
    System.out.println(format("\tFachdienst:         {0}", env.getInternet().getFdBaseUrl()));
    System.out.println(format("\tUser-Agent:         {0}", env.getInternet().getUserAgent()));
    System.out.println(
        format("\tAPI-Key:            {0}", maskSecret(env.getInternet().getXapiKey())));
    System.out.println(format("\tTSL:                {0}", env.getTslBaseUrl()));
    System.out.println(
        format("\tDiscovery Document: {0}", env.getInternet().getDiscoveryDocumentUrl()));
    System.out.println(format("\tIDP Client ID:      {0}", env.getInternet().getClientId()));
    System.out.println(format("\tIDP Redirect URL:   {0}", env.getInternet().getRedirectUrl()));

    System.out.println("----------------");
  }

  private String maskSecret(String secret) {
    if (secret.isEmpty() || secret.isBlank()) {
      return secret;
    }

    val startIdx = (int) (secret.length() * 0.3);
    val endIdx = (int) (secret.length() * 0.7);
    val maskedLength = endIdx - startIdx;

    val first = secret.substring(0, startIdx);
    val masked = new String(new char[maskedLength]).replace("\0", "*");
    val last = secret.substring(endIdx, secret.length());

    return format("{0}{1}{2}", first, masked, last);
  }
}
