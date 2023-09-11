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

package de.gematik.test.erezept.fhir.resources.erp;

import ca.uhn.fhir.model.api.annotation.*;
import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.values.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Extension;

/**
 * @see <a href="https://simplifier.net/erezept-workflow/erxchargeitem">ErxMedicationDispense</a>
 */
@Slf4j
@ResourceDef(name = "ChargeItem")
@SuppressWarnings({"java:S110"})
public class ErxChargeItem extends ChargeItem {

  /**
   * @return true if this ChargeItem is from newer Profiles and false otherwise
   */
  public boolean isFromNewProfiles() {
    return this.getMeta().getProfile().stream()
        .anyMatch(PatientenrechnungStructDef.CHARGE_ITEM::match);
  }

  public PrescriptionId getPrescriptionId() {
    return this.getIdentifier().stream()
        .filter(PrescriptionId::isPrescriptionId)
        .map(identifier -> new PrescriptionId(identifier.getValue()))
        .findFirst() // Prescription ID has cardinality of 1..1 anyway
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(),
                    ErpWorkflowNamingSystem.PRESCRIPTION_ID,
                    ErpWorkflowNamingSystem.PRESCRIPTION_ID_121));
  }

  public Optional<AccessCode> getAccessCode() {
    return this.getIdentifier().stream()
        .filter(AccessCode::isAccessCode)
        .map(identifier -> new AccessCode(identifier.getValue()))
        .findFirst(); // AccessCode has cardinality of 0..1 anyway
  }

  public KVNR getSubjectKvnr() {
    return KVNR.from(this.getSubject().getIdentifier().getValue());
  }

  public String getAssigner() {
    return this.getSubject().getIdentifier().getAssigner().getDisplay();
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

  public Optional<String> getReceiptReference() {
    return this.supportingInformation.stream()
        .filter(it -> ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE.match(it.getDisplay()))
        .map(Reference::getReference)
        .findFirst();
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
            ext ->
                ext.getUrl().equals(ErpWorkflowStructDef.MARKING_FLAG.getCanonicalUrl())
                    || ext.getUrl()
                        .equals(PatientenrechnungStructDef.MARKING_FLAG.getCanonicalUrl()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(),
                    ErpWorkflowStructDef.MARKING_FLAG,
                    PatientenrechnungStructDef.MARKING_FLAG));
  }

  public void removeContainedResources() {
    this.getContained().clear();
    this.supportingInformation.stream()
        .filter(ref -> ref.getReference().startsWith("#"))
        .forEach(
            ref ->
                ref.setReference(ref.getReference().replace("#", "Bundle/"))
                    .setDisplay(
                        AbdaErpPkvStructDef.PKV_ABGABEDATENSATZ
                            .getCanonicalUrl()) // hardcoded for now
                    .setType(null));
  }

  public void removeAccessCode() {
    this.getIdentifier().removeIf(AccessCode::isAccessCode);
  }

  public static ErxChargeItem fromChargeItem(ChargeItem adaptee) {
    val erxChargeItem = new ErxChargeItem();
    adaptee.copyValues(erxChargeItem);
    return erxChargeItem;
  }

  public static ErxChargeItem fromChargeItem(Resource adaptee) {
    return fromChargeItem((ChargeItem) adaptee);
  }
}
