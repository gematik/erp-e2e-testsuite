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

package de.gematik.test.erezept.fhir.util;

import java.util.function.Function;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;

public class FhirEntryReplacer {

  private FhirEntryReplacer() {
    throw new IllegalArgumentException("utility class");
  }

  /**
   * When we use the FHIR-Parser the profiled Parser will map the entries automatically. However,
   * once we copy a resource the child entries will be copied to their base classes. This method
   * helps to cast and to replace the resource within the entry to correct type
   *
   * @param childType is the expected resource type of the entry
   * @param entry is envelope entry containing the resource
   * @param childMapper is a function mapping the resource to its expected type
   * @return the entry-resource of its expected type
   * @param <C> the generic type of the entry-resource
   */
  public static <C extends Resource> C cast(
      Class<C> childType, BundleEntryComponent entry, Function<Resource, C> childMapper) {

    C ret = childMapper.apply(entry.getResource());
    if (!entry.getResource().getClass().equals(childType)) {
      // not yet of expected type; convert to correct type and replace the old instance
      entry.setResource(ret);
    }

    return ret;
  }
}
