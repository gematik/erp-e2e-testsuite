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
import java.util.List;
import picocli.CommandLine;

public class EgkParameter {

  @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
  private ExclusiveEgkIdentifierGroup egkIdentifier;

  public List<Egk> getEgks(SmartcardArchive sca) {
    return egkIdentifier.getEgks(sca);
  }
}
