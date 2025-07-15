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

package de.gematik.test.erezept.fhir.r4.kbv;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.r4.AbstractOrganization;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import de.gematik.test.erezept.fhir.values.BSNR;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

@SuppressWarnings({"java:S110"})
public class KbvMedicalOrganization extends AbstractOrganization implements ErpFhirResource {

  public Optional<BSNR> getBsnr() {
    return this.identifier.stream()
        .filter(KbvNamingSystem.BASE_BSNR::matches)
        .map(identifier -> new BSNR(identifier.getValue()))
        .findFirst();
  }

  public BSNR getBsnrOrThrow() {
    return this.getBsnr()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), KbvNamingSystem.BASE_BSNR));
  }

  @Override
  public String getDescription() {
    val anr = getBsnr().map(it -> format("BSNR: {0}", it.getValue())).orElse("BSNR: n/a");
    return format(
        "{0} ({1}) aus {2} {3}", this.getName(), anr, this.getPostalCode(), this.getCity());
  }

  public static KbvMedicalOrganization fromOrganization(Organization adaptee) {
    if (adaptee instanceof KbvMedicalOrganization medicalOrganization) {
      return medicalOrganization;
    } else {
      val kbvOrganization = new KbvMedicalOrganization();
      adaptee.copyValues(kbvOrganization);
      return kbvOrganization;
    }
  }

  public static KbvMedicalOrganization fromOrganization(Resource adaptee) {
    return fromOrganization((Organization) adaptee);
  }
}
