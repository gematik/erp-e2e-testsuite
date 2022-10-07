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

package de.gematik.test.erezept.fhir.extensions.erp;

import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
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
    val ext = new Extension(ErpStructureDefinition.GEM_SUPPLY_OPTIONS_TYPE.getCanonicalUrl());
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
}
