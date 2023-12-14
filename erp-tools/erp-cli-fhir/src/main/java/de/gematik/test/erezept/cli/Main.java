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

package de.gematik.test.erezept.cli;

import de.gematik.test.erezept.cli.cmd.ErpCliFhir;
import lombok.val;
import picocli.CommandLine;

public class Main {

  @SuppressWarnings("java:S4823")
  public static void main(String[] args) {
    val topLevelCommand =
        new CommandLine(new ErpCliFhir())
            .setSubcommandsCaseInsensitive(true)
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setAbbreviatedSubcommandsAllowed(true)
            .setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO));

    var exitCode = topLevelCommand.execute(args);
    System.exit(exitCode);
  }
}
