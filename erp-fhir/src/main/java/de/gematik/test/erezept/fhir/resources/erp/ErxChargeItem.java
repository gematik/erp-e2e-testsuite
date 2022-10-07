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

package de.gematik.test.erezept.fhir.resources.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.StructureDefinitionFixedUrls;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.Extension;

/**
 * @see <a href="https://simplifier.net/erezept-workflow/erxchargeitem">ErxMedicationDispense</a>
 */
@Slf4j
@ResourceDef(name = "ChargeItem", profile = StructureDefinitionFixedUrls.GEM_ERX_CHARGE_ITEM)
@SuppressWarnings({"java:S110"})
public class ErxChargeItem extends ChargeItem {

  public PrescriptionId getPrescriptionId() {
    return this.getIdentifier().stream()
        .filter(
            identifier ->
                ErpNamingSystem.PRESCRIPTION_ID.getCanonicalUrl().equals(identifier.getSystem()))
        .map(identifier -> new PrescriptionId(identifier.getValue()))
        .findFirst() // Prescription ID has cardinality of 1..1 anyways
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), ErpNamingSystem.PRESCRIPTION_ID));
  }

  public String getSubjectKvid() {
    // TODO: implement KVID as its own Type // NOSONAR still needs to be implemented
    return this.getSubject().getIdentifier().getValue();
  }

  public String getEntererTelematikId() {
    // TODO: implement TelematikID as its own Type // NOSONAR still needs to be implemented
    return this.getEnterer().getIdentifier().getValue();
  }

  public boolean hasInsuranceProvider() {
    val extUrl = "insuranceProvider";
    return this.getBoolFromMarkingFlag(extUrl);
  }

  public boolean hasSubsidy() {
    val extUrl = "subsidy";
    return this.getBoolFromMarkingFlag(extUrl);
  }

  public boolean hasTaxOffice() {
    val extUrl = "taxOffice";
    return this.getBoolFromMarkingFlag(extUrl);
  }

  private boolean getBoolFromMarkingFlag(String extensionUrl) {
    val markingFlag = this.getMarkingFlag();
    return markingFlag.getExtension().stream()
        .filter(ext -> ext.getUrl().equals(extensionUrl))
        .map(ext -> ext.getValue().castToBoolean(ext.getValue()).booleanValue())
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), extensionUrl));
  }

  private Extension getMarkingFlag() {
    return this.getExtension().stream()
        .filter(
            ext -> ext.getUrl().equals(ErpStructureDefinition.GEM_MARKING_FLAG.getCanonicalUrl()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), ErpStructureDefinition.GEM_MARKING_FLAG));
  }
}
