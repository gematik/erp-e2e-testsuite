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

package de.gematik.test.erezept.fhir.anonymizer;

import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.values.BaseANR;
import lombok.val;

public class KbvPractitionerAnonymizer implements Anonymizer<KbvPractitioner> {

  @Override
  public Class<KbvPractitioner> getType() {
    return KbvPractitioner.class;
  }

  @Override
  public void anonymize(AnonymizerContext ctx, KbvPractitioner resource) {
    ctx.anonymizeHumanNames(resource.getName());
    ctx.anonymizeAddresses(resource.getAddress());

    resource.getIdentifier().stream()
        .filter(BaseANR::hasValidIdentifier)
        .forEach(
            identifer -> {
              if (ctx.getIdentifierAnonymization() == AnonymizationType.BLACKING) {
                ctx.anonymize(identifer.getValueElement());
              } else {
                val qt = resource.getQualificationType();
                ctx.anonymize(
                    identifer.getValueElement(),
                    () -> BaseANR.randomFromQualification(qt).getValue());
              }
            });
  }
}
