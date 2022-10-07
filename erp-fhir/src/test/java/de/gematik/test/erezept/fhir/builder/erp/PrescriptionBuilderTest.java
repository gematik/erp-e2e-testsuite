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

package de.gematik.test.erezept.fhir.builder.erp;

import static java.text.MessageFormat.format;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.util.stream.Collectors;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.Before;
import org.junit.Test;

public class PrescriptionBuilderTest {

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void buildSimplePrescription() {
    val signed = new byte[] {1, 2, 3, 4};
    val param = PrescriptionBuilder.builder(signed);

    assertNotNull(param);
    val result = encodeAndValidate(param);
    assertTrue(result.isSuccessful());
  }

  // TODO: duplicate in FlowTypeBuilderTest -> outsource to testutil
  private ValidationResult encodeAndValidate(Parameters parameters) {
    // encode and check if encoded content is valid
    val xmlParameters = parser.encode(parameters, EncodingType.XML);
    val result = parser.validate(xmlParameters);
    printValidationResult(result);

    return result;
  }

  private void printValidationResult(final ValidationResult result) {
    if (!result.isSuccessful()) {
      // give me some hints if the encoded result is invalid
      val r =
          result.getMessages().stream()
              .map(m -> "(" + m.getLocationString() + ") " + m.getMessage())
              .collect(Collectors.joining("\n"));
      System.out.println(format("Errors: {0}\n{1}", result.getMessages().size(), r));
    }
  }
}
