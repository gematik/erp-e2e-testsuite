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

package de.gematik.test.erezept.fhir.extensions.erp;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Extension;

@Slf4j
@Getter
public class SupplyOptionsType {

  private final boolean onPremise;
  private final boolean delivery;
  private final boolean shipment;

  public SupplyOptionsType(boolean onPremise, boolean delivery, boolean shipment) {
    this.onPremise = onPremise;
    this.delivery = delivery;
    this.shipment = shipment;
  }

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
    val ext = new Extension(structDef.getCanonicalUrl());
    ext.addExtension(new Extension("onPremise", new BooleanType(onPremise)));
    ext.addExtension(new Extension("delivery", new BooleanType(delivery)));
    ext.addExtension(new Extension("shipment", new BooleanType(shipment)));
    return ext;
  }

  public static SupplyOptionsType createDefault() {
    return onPremise();
  }

  public static SupplyOptionsType onPremise() {
    return new SupplyOptionsType(true, false, false);
  }

  public static SupplyOptionsType delivery() {
    return new SupplyOptionsType(false, true, false);
  }

  public static SupplyOptionsType shipment() {

    return new SupplyOptionsType(false, false, true);
  }

  public static SupplyOptionsType getSupplyOptionType(String s) {
    if (s.equalsIgnoreCase("onPremise")) return SupplyOptionsType.onPremise();
    else if (s.equalsIgnoreCase("delivery")) return SupplyOptionsType.delivery();
    else if (s.equalsIgnoreCase("shipment")) return SupplyOptionsType.shipment();
    else return SupplyOptionsType.createDefault();
  }
}
