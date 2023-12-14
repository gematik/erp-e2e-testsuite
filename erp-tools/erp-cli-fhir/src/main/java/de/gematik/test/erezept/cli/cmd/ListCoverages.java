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

import de.gematik.test.erezept.fhir.values.InsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.Arrays;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "lscov",
    description = "list available and known coverages",
    mixinStandardHelpOptions = true)
public class ListCoverages implements Callable<Integer> {

  @CommandLine.Option(
      names = {"--coverage-type", "--insurance-type", "--type"},
      paramLabel = "<TYPE>",
      type = VersicherungsArtDeBasis.class,
      description =
          "The Type of the Insurance from ${COMPLETION-CANDIDATES} for the Coverage-Section")
  private VersicherungsArtDeBasis insuranceType;

  @Override
  public Integer call() throws Exception {
    var implementors = InsuranceCoverageInfo.getImplementors();

    if (insuranceType != null) {
      implementors =
          implementors.stream()
              .filter(
                  implementor ->
                      implementor.getEnumConstants()[0].getInsuranceType().equals(insuranceType))
              .toList();

      if (implementors.isEmpty()) {
        System.out.println(
            format(
                "No Insurances known for insurance type {0} ({1})",
                insuranceType, insuranceType.getDisplay()));
      }
    }

    implementors.forEach(
        implementor -> {
          System.out.println(
              format("--- {0} ---", implementor.getEnumConstants()[0].getInsuranceType()));
          Arrays.stream(implementor.getEnumConstants())
              .forEach(
                  ici -> {
                    System.out.println(format("  {0} : {1}", ici.getIknr(), ici.getName()));
                  });
        });
    return 0;
  }
}
