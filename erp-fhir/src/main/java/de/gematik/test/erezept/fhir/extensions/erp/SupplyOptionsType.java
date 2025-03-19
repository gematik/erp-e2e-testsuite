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

package de.gematik.test.erezept.fhir.extensions.erp;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Extension;

@Getter
@Slf4j
@RequiredArgsConstructor
public enum SupplyOptionsType {
  ON_PREMISE("onPremise"),
  DELIVERY("delivery"),
  SHIPMENT("shipment");

  private final String label;

  public Extension asExtension() {
    return asExtension(ErpWorkflowVersion.getDefaultVersion());
  }

  public Extension asExtension(ErpWorkflowVersion version) {
    ErpWorkflowStructDef structDef;
    if (version.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      structDef = ErpWorkflowStructDef.SUPPLY_OPTIONS_TYPE;
    } else {
      structDef = ErpWorkflowStructDef.SUPPLY_OPTIONS_TYPE_12;
    }
    val ext = structDef.asExtension();
    ext.addExtension(new Extension(label, new BooleanType(true)));
    for (val other : SupplyOptionsType.values()) {
      if (other != this) {
        ext.addExtension(new Extension(other.label, new BooleanType(false)));
      }
    }
    return ext;
  }

  public static SupplyOptionsType createDefault() {
    return SupplyOptionsType.ON_PREMISE;
  }

  public static SupplyOptionsType getSupplyOptionType(String label) {
    return Arrays.stream(SupplyOptionsType.values())
        .filter(it -> it.label.equalsIgnoreCase(label))
        .findFirst()
        .orElse(SupplyOptionsType.createDefault());
  }
}
