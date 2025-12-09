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

package de.gematik.test.erezept.fhir.r4.eu;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@ResourceDef(name = "PractitionerRole")
@SuppressWarnings({"java:S110"})
public class EuPractitionerRole extends PractitionerRole implements ErpFhirResource {

  public EuOrganizationProfession getOrgenanizationProfession() {
    return this.getCodeFirstRep().getCoding().stream()
        .filter(c -> c.getSystem().contains("urn:oid:"))
        .map(EuOrganizationProfession::fromCoding)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), "no Coding with system containing urn:oid: found"));
  }

  @Override
  public String getDescription() {
    return format("PractitionerRole for {0}", this.getPractitioner().getReference());
  }

  public static EuPractitionerRole fromPractitionerRole(PractitionerRole adaptee) {
    if (adaptee instanceof EuPractitionerRole euPractitionerRole) {
      return euPractitionerRole;
    } else {
      val euPractitionerRole = new EuPractitionerRole();
      adaptee.copyValues(euPractitionerRole);
      return euPractitionerRole;
    }
  }

  public static EuPractitionerRole fromPractitionerRole(Resource adaptee) {
    return fromPractitionerRole((PractitionerRole) adaptee);
  }
}
