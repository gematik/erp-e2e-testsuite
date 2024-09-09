/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.PatientenrechnungStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.references.dav.AbgabedatensatzReference;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TelematikID;
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

  public TelematikID getEntererTelematikId() {
    return TelematikID.from(this.getEnterer().getIdentifier());
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

  private Binary getContainedBinary() {
    return this.getContained().stream()
        .filter(resource -> resource.getResourceType().equals(ResourceType.Binary))
        .filter(
            resource ->
                ErpWorkflowStructDef.BINARY.match(resource.getMeta())
                    || ErpWorkflowStructDef.BINARY_12.match(resource.getMeta()))
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

    containedData
        .getMeta()
        .addProfile(
            ErpWorkflowStructDef.BINARY_12.getVersionedUrl(
                ErpWorkflowVersion.getDefaultVersion(), true));

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
