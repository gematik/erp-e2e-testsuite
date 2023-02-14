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

package de.gematik.test.erezept.screenplay.util;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import javax.annotation.Nullable;

public class FlowTypeUtil {

  private FlowTypeUtil() {
    throw new AssertionError("do not instantiate");
  }

  /**
   * The PrescriptionFlowType can be reasoned from the insurance kind (VersicherungsArtDeBasis) and
   * the PrescriptionAssignementKind. However, if a different WorkflowType is given via the
   * DataTable as the code, this one will overwrite the reasoned one.
   *
   * @param code is the String representation of the WorkflowType
   * @return a PrescriptionFlowType which was reasoned from Data; if provided via code (code !=
   *     null) then the PrescriptionFlowType from this code
   */
  public static PrescriptionFlowType getFlowType(
      @Nullable String code,
      VersicherungsArtDeBasis insuranceKind,
      PrescriptionAssignmentKind prescriptionKind) {
    PrescriptionFlowType ret;
    if (code != null) {
      ret = PrescriptionFlowType.fromCode(code);
    } else {
      ret = getFlowType(insuranceKind, prescriptionKind);
    }
    return ret;
  }

  private static PrescriptionFlowType getFlowType(
      VersicherungsArtDeBasis insuranceKind, PrescriptionAssignmentKind prescriptionKind) {
    PrescriptionFlowType ret;
    if (insuranceKind == VersicherungsArtDeBasis.GKV) {
      if (prescriptionKind == PrescriptionAssignmentKind.PHARMACY_ONLY) {
        ret = PrescriptionFlowType.FLOW_TYPE_160;
      } else if (prescriptionKind == PrescriptionAssignmentKind.DIRECT_ASSIGNMENT) {
        ret = PrescriptionFlowType.FLOW_TYPE_169;
      } else {
        // should not happen yet, but might be useful in the future!
        throw new FeatureNotImplementedException(
            format(
                "Issue prescription for {0} patient with {1}",
                insuranceKind.getCode(), prescriptionKind.getGerman()));
      }
    } else if (insuranceKind == VersicherungsArtDeBasis.PKV) {
      if (prescriptionKind == PrescriptionAssignmentKind.PHARMACY_ONLY) {
        ret = PrescriptionFlowType.FLOW_TYPE_200;
      } else if (prescriptionKind == PrescriptionAssignmentKind.DIRECT_ASSIGNMENT) {
        ret = PrescriptionFlowType.FLOW_TYPE_209;
      } else {
        // this case is possible but technically "not allowed" for PKV
        throw new FeatureNotImplementedException(
            format(
                "Issue prescription for {0} patient with {1}",
                insuranceKind.getCode(), prescriptionKind.getGerman()));
      }
    } else {
      ret = PrescriptionFlowType.FLOW_TYPE_160;
    }

    return ret;
  }
}
