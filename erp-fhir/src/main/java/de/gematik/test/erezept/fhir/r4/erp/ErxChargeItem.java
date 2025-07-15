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

package de.gematik.test.erezept.fhir.r4.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.PatientenrechnungStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.r4.dav.AbgabedatensatzReference;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

/**
 * @see <a href="https://simplifier.net/erezept-workflow/erxchargeitem">ErxMedicationDispense</a>
 */
@Slf4j
@ResourceDef(name = "ChargeItem")
@SuppressWarnings({"java:S110"})
public class ErxChargeItem extends ChargeItem {

  public PrescriptionId getPrescriptionId() {
    return this.getIdentifier().stream()
        .filter(PrescriptionId::isPrescriptionId)
        .map(PrescriptionId::from)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), ErpWorkflowNamingSystem.PRESCRIPTION_ID));
  }

  public Optional<AccessCode> getAccessCode() {
    return this.getIdentifier().stream()
        .filter(AccessCode::isAccessCode)
        .map(identifier -> AccessCode.from(identifier.getValue()))
        .findFirst();
  }

  public KVNR getSubjectKvnr() {
    return KVNR.from(this.getSubject().getIdentifier().getValue());
  }

  public TelematikID getEntererTelematikId() {
    return TelematikID.from(this.getEnterer().getIdentifier());
  }

  public boolean hasInsuranceProvider() {
    return this.getBoolFromMarkingFlag("insuranceProvider");
  }

  public boolean hasSubsidy() {
    return this.getBoolFromMarkingFlag("subsidy");
  }

  public boolean hasTaxOffice() {
    return this.getBoolFromMarkingFlag("taxOffice");
  }

  public Optional<String> getReceiptReference() {
    return this.supportingInformation.stream()
        .filter(it -> ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE.matches(it.getDisplay()))
        .map(Reference::getReference)
        .findFirst();
  }

  private boolean getBoolFromMarkingFlag(String extensionUrl) {
    val markingFlag = this.getMarkingFlag();
    return markingFlag.getExtension().stream()
        .filter(ext -> ext.getUrl().equals(extensionUrl))
        .map(ext -> ext.getValue().castToBoolean(ext.getValue()).booleanValue())
        .findFirst()
        .orElse(false);
  }

  private Extension getMarkingFlag() {
    return this.getExtension().stream()
        .filter(PatientenrechnungStructDef.MARKING_FLAG::matches)
        .findFirst()
        .orElse(new Extension()); // empty Extension as Null-Object-Pattern
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

  private Binary getContainedBinary() {
    return this.getContained().stream()
        .filter(resource -> resource.getResourceType().equals(ResourceType.Binary))
        .map(Binary.class::cast)
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "Contained Binary Resource"));
  }

  public byte[] getContainedBinaryData() {
    return getContainedBinary().getContent();
  }

  /**
   * This method is required for PUT /chargeitem operation: first you need to fetch an existing
   * chargeitem via GET /chargeitem, sign the DavBundle and then use this method to create a copy of
   * the original ChargeItem with a changed contained Binary
   *
   * @param reference for the contained Binary data
   * @param signedData representing the content of the contained binary
   * @return a copied ChargeItem with a changed contained Binary
   */
  public ErxChargeItem withChangedContainedBinaryData(
      AbgabedatensatzReference reference, byte[] signedData) {
    val containedData =
        new Binary().setContentType("application/pkcs7-mime").setContent(signedData);
    containedData.setId(reference.getReference(false));

    val ret = ErxChargeItem.fromChargeItem(this);
    ret.removeContainedResources();
    ret.addContained((Resource) containedData);

    // now fix the supporting information
    reference.makeContained();
    reference.setType(null);
    reference.setDisplay("Binary");
    ret.addSupportingInformation(reference);

    return ret;
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
