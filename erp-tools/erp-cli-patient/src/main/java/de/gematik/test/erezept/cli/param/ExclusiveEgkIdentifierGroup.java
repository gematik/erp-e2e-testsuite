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

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.cli.converter.StringListConverter;
import java.util.LinkedList;
import java.util.List;
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
    return kvnrs != null && !kvnrs.isEmpty();
  }

  public boolean hasIccsns() {
    return iccsns != null && !iccsns.isEmpty();
  }

  public List<Egk> getEgks(SmartcardArchive sca) {
    val ret = new LinkedList<Egk>();

    if (hasKvnrs()) {
      ret.addAll(this.kvnrs.stream().map(sca::getEgkByKvnr).toList());
    }

    if (hasIccsns()) {
      ret.addAll(this.iccsns.stream().map(sca::getEgkByICCSN).toList());
    }

    return ret;
  }
}
