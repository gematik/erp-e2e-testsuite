/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.cli.param;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.test.erezept.cli.converter.StringListConverter;
import de.gematik.test.erezept.cli.exceptions.CliException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.val;
import picocli.CommandLine;

public class ExclusiveEgkIdentifierGroup {

  @CommandLine.Option(
      names = {"--kvid", "--kvnr"},
      paramLabel = "<KVNR(s)>",
      type = String.class,
      converter = StringListConverter.class,
      description = "List of the KVNRs of the smartcards to use")
  private List<String> kvnrs;

  @CommandLine.Option(
      names = "--iccsn",
      paramLabel = "<ICCSN(s)>",
      type = String.class,
      converter = StringListConverter.class,
      description = "List of the ICCSNs of the smartcards to use")
  private List<String> iccsns;

  public boolean hasKvnrs() {
    return Optional.ofNullable(kvnrs).filter(it -> !it.isEmpty()).isPresent();
  }

  public boolean hasIccsns() {
    return Optional.ofNullable(iccsns).filter(it -> !it.isEmpty()).isPresent();
  }

  /**
   * This group is exclusive, so only one of the two lists can be set. If you choose to select KVNRs
   * by their ICCSNs, the KVNRs will be resolved from the SmartcardArchive. If you choose to select
   * KVNRs directly, the KVNRs will be used as is. In case the SmartcardArchive does not contain
   * kryptographic material for the KVNRs, authentication via GID will be chosen.
   *
   * @param sca the SmartcardArchive to resolve KVNRs from ICCSNs
   * @return a list of KVNRs, either directly from the KVNRs list or resolved from the ICCSNs list
   */
  public List<String> getKvnrs(SmartcardArchive sca) {
    val ret = new LinkedList<String>();

    if (hasKvnrs()) {
      ret.addAll(this.kvnrs);
    }

    if (hasIccsns()) {
      val iccsnToKvnr =
          sca.getConfigsFor(SmartcardType.EGK).stream()
              .collect(
                  Collectors.toMap(
                      SmartcardConfigDto::getIccsn,
                      SmartcardConfigDto::getIdentifier,
                      (first, second) -> first));

      this.iccsns.forEach(
          iccsn ->
              ret.add(
                  Optional.ofNullable(iccsnToKvnr.get(iccsn))
                      .orElseThrow(
                          () ->
                              new CliException(
                                  format(
                                      "No eGK with ICCSN {0} found in SmartcardArchive", iccsn)))));
    }

    return ret;
  }
}
