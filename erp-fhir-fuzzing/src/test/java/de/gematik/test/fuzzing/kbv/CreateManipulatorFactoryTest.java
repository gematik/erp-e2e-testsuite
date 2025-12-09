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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.fuzzing.kbv;

import static de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowCodeSystem.FLOW_TYPE_12;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CreateManipulatorFactoryTest extends ErpFhirParsingTest {

  Parameters create =
      parser.decode(
          Parameters.class,
          "<Parameters xmlns=\"http://hl7.org/fhir\"><parameter><name"
              + " value=\"workflowType\"/><valueCoding><system"
              + " value=\"https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_FlowType\"/><code"
              + " value=\"160\"/></valueCoding></parameter></Parameters>");

  @ParameterizedTest
  @ValueSource(
      strings = {
        "set Old-FlowType-System  as Value in Parameter.valueCoding",
        "set AVAILABILITY_STATUS-System  as Value in Parameter.valueCoding"
      })
  void shouldManipulateFlowType(String manipulatorDescription) {
    val manipulators = CreateManipulatorFactory.getCreateManipulators();

    val manipulator = findManipulator(manipulators, manipulatorDescription);
    assertNotNull(manipulator, "Manipulator for wrong FlowType should exist");
    assertTrue(ValidatorUtil.encodeAndValidate(parser, create).isSuccessful());
    manipulator.getParameter().accept(create);
    assertNotEquals(
        FLOW_TYPE_12.getCanonicalUrl(),
        create
            .getParameterFirstRep()
            .getValue()
            .castToCoding(create.getParameterFirstRep().getValue())
            .getSystem(),
        "No Fitting FlowType-System contained");
  }

  /**
   * Utility method to find a manipulator by name.
   *
   * @param manipulators List of NamedEnvelope objects
   * @param name The name of the manipulator to find
   * @return The NamedEnvelope if found, otherwise null
   */
  private NamedEnvelope<FuzzingMutator<Parameters>> findManipulator(
      List<NamedEnvelope<FuzzingMutator<Parameters>>> manipulators, String name) {
    return manipulators.stream().filter(m -> m.getName().equals(name)).findFirst().orElseThrow();
  }
}
