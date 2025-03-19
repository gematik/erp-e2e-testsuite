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

package de.gematik.test.erezept.fhir.r4;

import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.IKNR;

@SuppressWarnings({"java:S110"})
public abstract class InstitutionalOrganization extends AbstractOrganization {

  public IKNR getIknr() {
    return this.identifier.stream()
        .filter(
            identifier ->
                WithSystem.anyOf(DeBasisProfilNamingSystem.IKNR_SID, DeBasisProfilNamingSystem.IKNR)
                    .matches(identifier))
        .map(IKNR::from)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(),
                    DeBasisProfilNamingSystem.IKNR,
                    DeBasisProfilNamingSystem.IKNR_SID));
  }
}
