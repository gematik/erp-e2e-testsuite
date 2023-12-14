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

package de.gematik.test.erezept.fhir.resources.kbv;

import de.gematik.test.erezept.fhir.resources.InstitutionalOrganization;
import lombok.val;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

@SuppressWarnings({"java:S110"})
public class AssignerOrganization extends InstitutionalOrganization {

  public static AssignerOrganization fromOrganization(Organization adaptee) {
    if (adaptee instanceof AssignerOrganization assignerOrganization) {
      return assignerOrganization;
    } else {
      val kbvOrganization = new AssignerOrganization();
      adaptee.copyValues(kbvOrganization);
      return kbvOrganization;
    }
  }

  public static AssignerOrganization fromOrganization(Resource adaptee) {
    return fromOrganization((Organization) adaptee);
  }
}
