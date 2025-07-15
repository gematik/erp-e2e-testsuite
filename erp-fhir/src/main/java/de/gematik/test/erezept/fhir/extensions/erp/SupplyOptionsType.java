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

package de.gematik.test.erezept.fhir.extensions.erp;

import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
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
    val ext = ErpWorkflowStructDef.SUPPLY_OPTIONS_TYPE.asExtension();
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
