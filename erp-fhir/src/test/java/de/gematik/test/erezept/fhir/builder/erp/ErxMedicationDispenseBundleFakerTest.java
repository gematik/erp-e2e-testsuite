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
 */

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.parser.profiles.ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

@SetSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE, value = "1.3.0") // no longer required in 1.4.0
class ErxMedicationDispenseBundleFakerTest extends ErpFhirParsingTest {

  @Test
  void buildFakeMedicationDispenseBundleBuilder() {
    val bundle = ErxMedicationDispenseBundleFaker.build().fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeMedicationDispenseBundleBuilderWithAmount() {
    val bundle = ErxMedicationDispenseBundleFaker.build().withAmount(4).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertEquals(4, bundle.getEntry().size());
    assertTrue(result.isSuccessful());
  }
}
