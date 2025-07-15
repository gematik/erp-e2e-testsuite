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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.eml.fhir.r4;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import org.hl7.fhir.r4.model.Organization;

@SuppressWarnings("java:S110")
public class EpaOrganisation extends Organization {

  public TelematikID getTelematikId() {
    return this.getIdentifier().stream()
        .filter(DeBasisProfilNamingSystem.TELEMATIK_ID_SID::matches)
        .map(identifier -> TelematikID.from(identifier.getValue()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), DeBasisProfilNamingSystem.TELEMATIK_ID_SID));
  }
}
