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

import de.gematik.test.erezept.cli.param.ExclusiveEgkIdentifierGroup;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardFactory;
import java.util.List;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
    name = "lsp",
    description = "list available patients/eGKs",
    mixinStandardHelpOptions = true)
public class ListPatients implements Callable<Integer> {

  // TODO: this ICCSN does not have proper certificates
  private static final List<String> FILTER_ICCSNS = List.of("80276881040001935352");

  @CommandLine.ArgGroup(exclusive = true, multiplicity = "0..1")
  private ExclusiveEgkIdentifierGroup egkIdentifier;

  @CommandLine.Option(
      names = "--cert",
      required = false,
      description = "Print the complete X509 Certificate")
  private boolean printCertificate = false;

  @Override
  public Integer call() throws Exception {
    val sca = SmartcardFactory.getArchive();

    val smartcards = hasFilter() ? egkIdentifier.getEgks(sca) : sca.getEgkCards();

    smartcards.stream()
        .filter(egk -> !FILTER_ICCSNS.contains(egk.getIccsn()))
        .forEach(this::printInformation);

    return 0;
  }

  private boolean hasFilter() {
    return egkIdentifier != null && (egkIdentifier.hasKvnrs() || egkIdentifier.hasIccsns());
  }

  private void printInformation(Egk egk) {
    if (printCertificate) {
      System.out.println(egk.getAutCertificate().getX509Certificate());
    } else {
      System.out.println(format("{0} : {1}", egk.getIccsn(), egk.getKvnr()));
      System.out.println(format("\tOwner:        {0}", egk.getOwner().getOwnerName()));
      System.out.println(format("\tKrankenkasse: {0}", egk.getOwner().getOrganization()));
    }

    System.out.println("----------------");
  }
}
