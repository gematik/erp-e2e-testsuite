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

import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.KVNR;

public class KbvPatientAnonymizer implements Anonymizer<KbvPatient> {

  @Override
  public Class<KbvPatient> getType() {
    return KbvPatient.class;
  }

  @Override
  public void anonymize(AnonymizerContext ctx, KbvPatient resource) {
    ctx.anonymizeHumanNames(resource.getName());
    ctx.anonymizeAddresses(resource.getAddress());
    ctx.anonymize(resource.getBirthDateElement());

    if (ctx.getIdentifierAnonymization() == AnonymizationType.BLACKING) {
      resource.getGkvIdentifier().ifPresent(id -> ctx.anonymize(id.getValueElement()));
      resource.getPkvIdentifier().ifPresent(id -> ctx.anonymize(id.getValueElement()));
    } else {
      resource
          .getGkvIdentifier()
          .ifPresent(id -> ctx.anonymize(id.getValueElement(), () -> KVNR.random().getValue()));
      resource
          .getPkvIdentifier()
          .ifPresent(id -> ctx.anonymize(id.getValueElement(), () -> KVNR.random().getValue()));
    }
  }
}
