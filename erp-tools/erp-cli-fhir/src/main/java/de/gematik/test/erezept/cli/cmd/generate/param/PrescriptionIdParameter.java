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

package de.gematik.test.erezept.cli.cmd.generate.param;

import de.gematik.test.erezept.cli.converter.FlowTypeConverter;
import de.gematik.test.erezept.cli.converter.PrescriptionIdConverter;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.Objects;
import picocli.CommandLine;

public class PrescriptionIdParameter {

  @CommandLine.Option(
      names = "--flowtype",
      type = PrescriptionFlowType.class,
      converter = FlowTypeConverter.class,
      description = "Define the prescription flow type for a random prescription id")
  private PrescriptionFlowType flowType;

  @CommandLine.Option(
      names = "--prescriptionid",
      type = PrescriptionId.class,
      converter = PrescriptionIdConverter.class,
      description = "Define the prescriptionid")
  private PrescriptionId prescriptionId;

  public PrescriptionId getPrescriptionId() {
    if (prescriptionId != null) {
      return prescriptionId;
    } else
      return PrescriptionId.random(
          Objects.requireNonNullElse(flowType, PrescriptionFlowType.FLOW_TYPE_200));
  }
}
