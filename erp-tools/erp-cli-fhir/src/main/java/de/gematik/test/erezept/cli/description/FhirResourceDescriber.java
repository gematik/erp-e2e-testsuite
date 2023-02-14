/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.cli.description;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.resources.ErpFhirResource;
import org.hl7.fhir.r4.model.Resource;

public class FhirResourceDescriber {

  public <R extends Resource> String acceptResource(R resource) {
    if (resource instanceof ErpFhirResource erpFhirResource) {
      return acceptDescribedResource(erpFhirResource);
    } else {
      return format(
          "Base Resource of type {0} with ID {1}", resource.getResourceType(), resource.getId());
    }
  }

  private String acceptDescribedResource(ErpFhirResource resource) {
    return resource.getDescription();
  }
}
