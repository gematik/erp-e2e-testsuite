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

package de.gematik.test.erezept.primsys.model;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.client.usecases.ChargeItemPostCommand;
import de.gematik.test.erezept.client.usecases.ChargeItemPutCommand;
import de.gematik.test.erezept.client.usecases.ICommand;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenBuilder;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvDispensedMedicationBuilder;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.dav.DavInvoice;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvAbgabedatenBundle;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvDispensedMedication;
import de.gematik.test.erezept.fhir.r4.dav.PharmacyOrganization;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.data.ChargeItemDto;
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto;
import de.gematik.test.erezept.primsys.mapping.InvoiceDataMapper;
import de.gematik.test.erezept.primsys.rest.data.InvoiceData;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.core.Response;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public class ChargeItemUseCase {

  private final Pharmacy pharmacy;

  public ChargeItemUseCase(Pharmacy pharmacy) {
    this.pharmacy = pharmacy;
  }

  public Response postChargeItem(String taskId, @Nullable InvoiceData invoiceData) {
    return postChargeItem(taskId, this.createDavInvoice(invoiceData));
  }

  public Response putChargeItem(
      String taskId, String accessCode, @Nullable InvoiceData invoiceData) {
    return putChargeItem(taskId, accessCode, this.createDavInvoice(invoiceData));
  }

  public Response postChargeItem(String taskId, DavInvoice davInvoice) {
    val chargeItemPair = this.createChargeItem(taskId, davInvoice);
    val cmd = new ChargeItemPostCommand(chargeItemPair.getLeft(), chargeItemPair.getRight());
    return executeCommand(cmd);
  }

  public Response putChargeItem(String taskId, String accessCode, DavInvoice davInvoice) {
    val cmdGet =
        new ChargeItemGetByIdCommand(
            PrescriptionId.from(taskId), AccessCode.fromString(accessCode));
    val original = pharmacy.erpRequest(cmdGet).getExpectedResource().getChargeItem();

    val dispenseData = this.getDispensedData(taskId);
    val pharmacyOrg = pharmacy.createPharmacyOrganization();
    val davBundle =
        this.createAbgabedatenBundle(dispenseData.getPrescriptionId(), davInvoice, pharmacyOrg);

    val davXml = pharmacy.getClient().encode(davBundle, EncodingType.XML);
    val signedDavBundle = pharmacy.signDocument(davXml);
    val changedChargeItem =
        original.withChangedContainedBinaryData(davBundle.getReference(), signedDavBundle);

    val cmd = new ChargeItemPutCommand(AccessCode.fromString(accessCode), changedChargeItem);
    return executeCommand(cmd);
  }

  private Response executeCommand(ICommand<ErxChargeItem> cmd) {
    val erpResponse = pharmacy.erpRequest(cmd);
    val chargeItem = erpResponse.getExpectedResource();

    val dto = new ChargeItemDto();
    dto.setId(chargeItem.getId());
    dto.setPrescriptionId(chargeItem.getPrescriptionId().getValue());
    chargeItem.getAccessCode().ifPresent(ac -> dto.setAccessCode(ac.getValue()));
    dto.setEntererTelematikId(chargeItem.getEntererTelematikId().getValue());
    dto.setSubjectKvnr(chargeItem.getSubjectKvnr().getValue());
    dto.setEntered(chargeItem.getEnteredDate());

    return Response.status(erpResponse.getStatusCode()).entity(dto).build();
  }

  private DavInvoice createDavInvoice(@Nullable InvoiceData invoiceData) {
    DavInvoice davInvoice;
    if (invoiceData == null) {
      davInvoice = new InvoiceDataMapper(new InvoiceData()).convert();
    } else {
      davInvoice = new InvoiceDataMapper(invoiceData).convert();
    }
    return davInvoice;
  }

  /**
   * create a completely new charge item
   *
   * @param taskId referencing the prescription
   * @param davInvoice for the chargeitem to build
   * @return a pair with charge item and the corresponding secret
   */
  private Pair<ErxChargeItem, Secret> createChargeItem(String taskId, DavInvoice davInvoice) {
    val dispenseData = this.getDispensedData(taskId);
    val acceptData = dispenseData.getAcceptData();
    val pharmacyOrg = pharmacy.createPharmacyOrganization();
    val davBundle =
        this.createAbgabedatenBundle(dispenseData.getPrescriptionId(), davInvoice, pharmacyOrg);

    val davXml = pharmacy.getClient().encode(davBundle, EncodingType.XML);
    val signedDavBundle = pharmacy.signDocument(davXml);

    val chargeItem =
        ErxChargeItemBuilder.forPrescription(PrescriptionId.from(dispenseData.getPrescriptionId()))
            .version(PatientenrechnungVersion.V1_0_0) // NOTE: this will change in the future!!
            .status("billable")
            .enterer(pharmacy.getSmcb().getTelematikId())
            .subject(KVNR.from(acceptData.getForKvnr()), acceptData.getInsurance().getName())
            .receipt(ErxReceipt.asReferenceFromId(dispenseData.getReceipt()))
            .verordnung(acceptData.getPrescriptionReference())
            .abgabedatensatz(davBundle.getReference(), signedDavBundle)
            .build();
    return Pair.of(chargeItem, Secret.fromString(dispenseData.getSecret()));
  }

  private DispensedMedicationDto getDispensedData(String taskId) {
    return ActorContext.getInstance().getDispensedMedications().stream()
        .filter(dd -> dd.getPrescriptionId().equals(taskId))
        .findFirst()
        .orElseThrow(
            () ->
                ErrorResponseBuilder.createInternalErrorException(
                    404, format("no dispensed medication for PrescriptionId {0} found", taskId)));
  }

  private DavPkvAbgabedatenBundle createAbgabedatenBundle(
      String prescriptionId, DavInvoice davInvoice, PharmacyOrganization pharmacyOrg) {
    val dispMed = createDispensedMedicationFor(prescriptionId, davInvoice, pharmacyOrg);
    return DavPkvAbgabedatenBuilder.builder(PrescriptionId.from(prescriptionId))
        .invoice(davInvoice)
        .medication(dispMed)
        .pharmacy(pharmacyOrg)
        .build();
  }

  private DavPkvDispensedMedication createDispensedMedicationFor(
      String prescriptionId, DavInvoice davInvoice, PharmacyOrganization pharmacyOrg) {
    return DavPkvDispensedMedicationBuilder.builder()
        .prescription(PrescriptionId.from(prescriptionId))
        .pharmacy(pharmacyOrg)
        .invoice(davInvoice)
        .build();
  }
}
