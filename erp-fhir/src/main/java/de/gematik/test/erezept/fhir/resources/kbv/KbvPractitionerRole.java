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

package de.gematik.test.erezept.fhir.resources.kbv;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.resources.ErpFhirResource;
import de.gematik.test.erezept.fhir.values.AsvTeamNumber;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.PractitionerRole;

@Slf4j
@ResourceDef(name = "PractitionerRole")
@SuppressWarnings({"java:S110"})
public class KbvPractitionerRole extends PractitionerRole implements ErpFhirResource {

  public AsvTeamNumber getTeamNumber() {
    return Optional.ofNullable(this.getOrganization().getIdentifier())
        .filter(DeBasisNamingSystem.TEAMNUMMER::match)
        .map(identifier -> AsvTeamNumber.from(identifier.getValue()))
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), DeBasisNamingSystem.TEAMNUMMER));
  }

  @Override
  public String getDescription() {
    return format("PractitionerRole for {0}", this.getPractitioner().getReference());
  }
}
