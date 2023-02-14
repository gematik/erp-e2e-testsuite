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

package de.gematik.test.erezept.fhir.valuesets;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * <br>
 * <b>Profile:</b> de.gematik.erezept-workflow.r4 (1.0.3) <br>
 * <b>File:</b> ValueSet-FLOWTYPE.json <br>
 * <br>
 * <b>Publisher:</b> gematik GmbH <br>
 * <b>Published:</b> None <br>
 * <b>Status:</b> draft
 */
@Getter
public enum PrescriptionFlowType implements IValueSet {
  FLOW_TYPE_160("160", "Muster 16 (Apothekenpflichtige Arzneimittel)"),
  FLOW_TYPE_169("169", "Muster 16 (Direkte Zuweisung)"),
  FLOW_TYPE_200("200", "PKV (Apothekenpflichtige Arzneimittel)"),
  FLOW_TYPE_209("209", "PKV (Direkte Zuweisung)");

  public static final ErpWorkflowCodeSystem CODE_SYSTEM = ErpWorkflowCodeSystem.FLOW_TYPE;
  public static final String VERSION = "1.0.3";
  public static final String DESCRIPTION =
      "Type of the prescription according to the 'Muster 16' forms";
  public static final String PUBLISHER = "gematik GmbH";

  private final String code;
  private final String display;
  private final String definition;

  PrescriptionFlowType(String code, String display) {
    this(code, display, "N/A definition in profile");
  }

  PrescriptionFlowType(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  public boolean isDirectAssignment() {
    return this.equals(FLOW_TYPE_169) || this.equals(FLOW_TYPE_209);
  }

  public static PrescriptionFlowType fromCode(@NonNull String value) {
    return Arrays.stream(PrescriptionFlowType.values())
        .filter(pft -> pft.code.equals(value))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(PrescriptionFlowType.class, value));
  }

  public static PrescriptionFlowType fromPrescriptionId(@NonNull PrescriptionId prescriptionId) {
    val rawFlowType = prescriptionId.getValue().substring(0, 3);
    return PrescriptionFlowType.fromCode(rawFlowType);
  }

  public ErpWorkflowCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  @Override
  public String toString() {
    return format("{0}", display);
  }
}
