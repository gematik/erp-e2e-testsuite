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

package de.gematik.test.erezept.cli.cmd.generate.param;

import de.gematik.test.erezept.cli.converter.KvnrConverter;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import picocli.CommandLine;

public class KvnrParameter implements BaseResourceParameter {

  @CommandLine.Option(
      names = {"--kvid", "--kvnr"},
      paramLabel = "<KVNR>",
      type = KVNR.class,
      converter = KvnrConverter.class,
      description = "The KVNR of the patient")
  private KVNR kvnr;

  @CommandLine.Option(
      names = {"--insurance-type"},
      paramLabel = "<TYPE>",
      type = VersicherungsArtDeBasis.class,
      description =
          "The Type of the Insurance from ${COMPLETION-CANDIDATES} for the Patient-Section")
  private VersicherungsArtDeBasis versicherungsArt;

  public KVNR getKvnr() {
    return this.getOrDefault(kvnr, KVNR::random);
  }

  public VersicherungsArtDeBasis getInsuranceType() {
    return getInsuranceType(VersicherungsArtDeBasis.GKV);
  }

  public VersicherungsArtDeBasis getInsuranceType(VersicherungsArtDeBasis defaultValue) {
    return getOrDefault(versicherungsArt, () -> defaultValue);
  }

  public IdentifierTypeDe getIdentifierTypeDe() {
    return getInsuranceType().equals(VersicherungsArtDeBasis.GKV)
        ? IdentifierTypeDe.GKV
        : IdentifierTypeDe.PKV;
  }
}
