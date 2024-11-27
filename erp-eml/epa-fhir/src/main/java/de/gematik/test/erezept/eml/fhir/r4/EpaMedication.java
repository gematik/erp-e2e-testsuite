/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.eml.fhir.r4;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.parser.profiles.EpaStructDef;
import de.gematik.test.erezept.eml.fhir.values.RxPrescriptionId;
import java.util.Optional;
import org.hl7.fhir.r4.model.Medication;

@SuppressWarnings("java:S110")
public class EpaMedication extends Medication {

  public Optional<PZN> getPzn() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .map(PZN::from)
        .findFirst();
  }

  public Optional<ASK> getAsk() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.ASK::matches)
        .map(ASK::from)
        .findFirst();
  }

  public Optional<ATC> getAtc() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.ATC::matches)
        .map(ATC::from)
        .findFirst();
  }

  public Optional<RxPrescriptionId> getRxPrescriptionId() {
    return this.getExtension().stream()
        .filter(
            extension ->
                extension.getUrl().matches(EpaStructDef.RX_PRESCRIPTION_ID.getCanonicalUrl()))
        .map(ext -> RxPrescriptionId.from(ext.getValue().castToIdentifier(ext.getValue())))
        .findFirst();
  }
}
