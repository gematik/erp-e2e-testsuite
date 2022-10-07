/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.primsys;

import de.gematik.test.erezept.primsys.cli.*;
import de.gematik.test.erezept.primsys.cli.KthonRunner;
import de.gematik.test.erezept.primsys.cli.KthonValidator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
public class KonnektathonMain {

  private static final String APP_NAME = "KTHON Testdaten Generator";
  private static final String VERSION = "0.1.0";

  @SuppressWarnings("java:S4823")
  public static void main(String[] args) {

    val topLevelCommand =
        new CommandLine(
                CommandLine.Model.CommandSpec.create()
                    .name(APP_NAME)
                    .version(VERSION)
                    .mixinStandardHelpOptions(true))
            .addSubcommand("testdaten", new KthonRunner())
            .addSubcommand("create", new KbvBundleCreator())
            .addSubcommand("fakexamples", new FhirFakerGenerator())
            .addSubcommand("dispense", new MedicationDispenser())
            .addSubcommand("validate", new KthonValidator())
            .addSubcommand("pspids", new PharmacyIdentifiers())
            .setSubcommandsCaseInsensitive(true)
            .setAbbreviatedSubcommandsAllowed(true)
            .setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.AUTO));

    var exitCode = topLevelCommand.execute(args);
    System.exit(exitCode);
  }
}
