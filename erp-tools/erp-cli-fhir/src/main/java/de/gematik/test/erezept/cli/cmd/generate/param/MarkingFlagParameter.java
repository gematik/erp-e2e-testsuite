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

package de.gematik.test.erezept.cli.cmd.generate.param;

import de.gematik.test.erezept.fhir.extensions.erp.MarkingFlag;
import lombok.Getter;
import picocli.CommandLine;

@Getter
public class MarkingFlagParameter {

  @CommandLine.Option(
      names = {"--insurance"},
      type = Boolean.class,
      defaultValue = "false",
      description = "MarkingFlag for insurance provider (default=${DEFAULT-VALUE})")
  @Getter
  private Boolean insuranceProvider = false;

  @CommandLine.Option(
      names = {"--subsidy"},
      type = Boolean.class,
      defaultValue = "false",
      description = "MarkingFlag for the subsidy (default=${DEFAULT-VALUE})")
  @Getter
  private Boolean subsidy = false;

  @CommandLine.Option(
      names = {"--taxoffice"},
      type = Boolean.class,
      defaultValue = "false",
      description = "MarkingFlag for tax office (default=${DEFAULT-VALUE})")
  @Getter
  private Boolean taxOffice = false;

  public MarkingFlag createFlags() {
    return MarkingFlag.with(insuranceProvider, subsidy, taxOffice);
  }
}
