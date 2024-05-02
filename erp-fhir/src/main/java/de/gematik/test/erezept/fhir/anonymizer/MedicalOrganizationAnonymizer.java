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

package de.gematik.test.erezept.fhir.anonymizer;

import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.BSNR;
import lombok.val;
import org.hl7.fhir.r4.model.ContactPoint;

public class MedicalOrganizationAnonymizer implements Anonymizer<MedicalOrganization> {

  @Override
  public Class<MedicalOrganization> getType() {
    return MedicalOrganization.class;
  }

  @Override
  public void anonymize(AnonymizerContext ctx, MedicalOrganization resource) {
    ctx.anonymizeAddresses(resource.getAddress());
    resource.getTelecom().stream().map(ContactPoint::getValueElement).forEach(ctx::anonymize);

    ctx.anonymize(resource.getNameElement());
    resource.getIdentifier().stream()
        .filter(KbvNamingSystem.BASE_BSNR::match)
        .forEach(
            id -> {
              val anonymizdBsnr = BSNR.random();
              ctx.anonymize(id.getValueElement(), anonymizdBsnr::getValue);
            });
  }
}
