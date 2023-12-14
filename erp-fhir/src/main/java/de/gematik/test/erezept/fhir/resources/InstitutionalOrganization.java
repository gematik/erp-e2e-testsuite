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

package de.gematik.test.erezept.fhir.resources;

import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.values.IKNR;

@SuppressWarnings({"java:S110"})
public abstract class InstitutionalOrganization extends AbstractOrganization {

  public IKNR getIknr() {
    return this.identifier.stream()
        .filter(
            identifier ->
                identifier
                        .getSystem()
                        .equals(
                            DeBasisNamingSystem.IKNR.getCanonicalUrl()) // old IKNR naming system
                    || identifier
                        .getSystem()
                        .equals(
                            DeBasisNamingSystem.IKNR_SID
                                .getCanonicalUrl())) // or new IKNR naming system
        .map(identifer -> new IKNR(identifer.getValue()))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), DeBasisNamingSystem.IKNR));
  }
}
