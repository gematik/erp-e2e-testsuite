/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.resources.dav;

import de.gematik.test.erezept.fhir.resources.InstitutionalOrganization;
import lombok.val;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

@SuppressWarnings({"java:S110"})
public class PharmacyOrganization extends InstitutionalOrganization {

  public static PharmacyOrganization fromOrganization(Organization adaptee) {
    val kbvOrganization = new PharmacyOrganization();
    adaptee.copyValues(kbvOrganization);
    return kbvOrganization;
  }

  public static PharmacyOrganization fromOrganization(Resource adaptee) {
    return fromOrganization((Organization) adaptee);
  }
}