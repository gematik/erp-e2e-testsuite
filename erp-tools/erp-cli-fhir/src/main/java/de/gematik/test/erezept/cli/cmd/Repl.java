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

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Slf4j
@Command(
    name = "repl",
    description = "interactive (Read-eval-print Loop) Mode",
    mixinStandardHelpOptions = true)
public class Repl implements Callable<Integer> {

  private static final List<String> STOP_CMDS = List.of("exit", "quit");
  @Spec private CommandSpec cmdSpec;

  @Override
  public Integer call() throws Exception {
    val userString =
        format("[{0}@{1}]> ", System.getProperty("user.name"), cmdSpec.parent().name());
    cmdSpec.parent().removeSubcommand("repl");

    val scanner = new Scanner(System.in); // Create a Scanner object
    val printer = System.out;

    var isRunning = true;
    while (isRunning) {
      printer.print(userString);
      val input = scanner.nextLine(); // Read user input
      if (shouldStopOn(input)) {
        isRunning = false;
      } else {
        val tokens = input.split(" ");
        if (tokens.length > 0) {
          val rc = cmdSpec.parent().commandLine().execute(tokens);
          if (rc != 0) {
            printer.println(); // this println is required
          }
        }
      }
    }

    return 0;
  }

  private boolean shouldStopOn(String input) {
    return STOP_CMDS.stream().anyMatch(stopCmd -> stopCmd.equalsIgnoreCase(input));
  }
}
