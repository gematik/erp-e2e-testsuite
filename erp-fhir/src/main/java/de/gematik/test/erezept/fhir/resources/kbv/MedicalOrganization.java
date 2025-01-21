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

import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.resources.AbstractOrganization;
import de.gematik.test.erezept.fhir.resources.ErpFhirResource;
import de.gematik.test.erezept.fhir.values.BSNR;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

@SuppressWarnings({"java:S110"})
public class MedicalOrganization extends AbstractOrganization implements ErpFhirResource {

  public Optional<BSNR> getBsnrOptional() {
    return this.identifier.stream()
        .filter(KbvNamingSystem.BASE_BSNR::match)
        .map(identifier -> new BSNR(identifier.getValue()))
        .findFirst();
  }

  public BSNR getBsnr() {
    return this.getBsnrOptional()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), KbvNamingSystem.BASE_BSNR));
  }

  @Override
  public String getDescription() {
    return format(
        "{0} (BSNR: {1}) aus {2} {3}",
        this.getName(), getBsnr().getValue(), this.getPostalCode(), this.getCity());
  }

  public static MedicalOrganization fromOrganization(Organization adaptee) {
    if (adaptee instanceof MedicalOrganization medicalOrganization) {
      return medicalOrganization;
    } else {
      val kbvOrganization = new MedicalOrganization();
      adaptee.copyValues(kbvOrganization);
      return kbvOrganization;
    }
  }

  public static MedicalOrganization fromOrganization(Resource adaptee) {
    return fromOrganization((Organization) adaptee);
  }
}
