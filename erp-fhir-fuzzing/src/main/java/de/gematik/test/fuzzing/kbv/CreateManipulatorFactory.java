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

import static de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowCodeSystem.*;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateManipulatorFactory {

  public static List<NamedEnvelope<FuzzingMutator<Parameters>>> getCreateManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<Parameters>>>();

    manipulators.add(
        NamedEnvelope.of(
            "set Old-FlowType-System  as Value in Parameter.valueCoding",
            b ->
                b.getParameterFirstRep()
                    .getValue()
                    .castToCoding(b.getParameterFirstRep().getValue())
                    .setSystem(FLOW_TYPE.getCanonicalUrl())));
    manipulators.add(
        NamedEnvelope.of(
            "set AVAILABILITY_STATUS-System  as Value in Parameter.valueCoding",
            b ->
                b.getParameterFirstRep()
                    .getValue()
                    .castToCoding(b.getParameterFirstRep().getValue())
                    .setSystem(AVAILABILITY_STATUS.getCanonicalUrl())));
    return manipulators;
  }
}
