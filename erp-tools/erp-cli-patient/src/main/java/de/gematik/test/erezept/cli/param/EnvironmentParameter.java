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

package de.gematik.test.erezept.cli.param;

import de.gematik.test.erezept.cli.cfg.ErpEnvironmentsConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import lombok.val;
import picocli.CommandLine;

public class EnvironmentParameter {

  @CommandLine.Option(
      names = {"--environment", "--env"},
      paramLabel = "<TI ENV>",
      type = String.class,
      description =
          "The TI-Environment where the operation should be performed (default=${DEFAULT-VALUE})")
  private String env = "TU";

  public EnvironmentConfiguration getEnvironment() {
    val cfg = new ErpEnvironmentsConfiguration();
    return cfg.getEnvironment(env);
  }
}
