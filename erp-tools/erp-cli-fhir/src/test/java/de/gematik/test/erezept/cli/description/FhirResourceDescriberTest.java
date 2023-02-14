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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

class FhirResourceDescriberTest {

  @Test
  void shouldAcceptDescribedResource() {
    val describer = new FhirResourceDescriber();
    val kbvBundle = KbvErpBundleBuilder.faker().build();
    val description = describer.acceptResource(kbvBundle);
    assertEquals(kbvBundle.getDescription(), description);
  }

  @Test
  void shouldCreateDefaultDescriptionForNonDescribed() {
    val describer = new FhirResourceDescriber();
    val bundle = new Bundle().setId("123");
    val description = describer.acceptResource(bundle);
    assertTrue(description.contains("123"));
    assertTrue(description.contains("Base Resource"));
  }
}
