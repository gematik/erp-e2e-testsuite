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

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.client.usecases.ChargeItemPostCommand;
import de.gematik.test.erezept.fhir.builder.dav.DavAbgabedatenBuilder;
import de.gematik.test.erezept.fhir.builder.dav.DavDispensedMedicationBuilder;
import de.gematik.test.erezept.fhir.builder.dav.PharmacyOrganizationBuilder;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.references.erp.ErxReceiptReference;
import de.gematik.test.erezept.fhir.resources.dav.DavInvoice;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.util.OperationOutcomeWrapper;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.primsys.mapping.InvoiceDataConverter;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import de.gematik.test.erezept.primsys.rest.data.InvoiceData;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;

import javax.annotation.Nullable;
import java.util.HashMap;

import static java.text.MessageFormat.format;

public class CreateChargeItemUseCase {

  public Response postChargeItem(Pharmacy pharmacy, String taskId, @Nullable InvoiceData invoiceData) {
    DavInvoice davInvoice;
    if (invoiceData == null) {
      davInvoice = new InvoiceDataConverter().convert();
    } else {
      davInvoice = new InvoiceDataConverter(invoiceData).convert();
    }
    return postChargeItem(pharmacy, taskId, davInvoice);
  }

  public Response postChargeItem(Pharmacy pharmacy, String taskId, DavInvoice davInvoice) {
    val dispenseData =
        ActorContext.getInstance().getDispensedMedications().stream()
            .filter(dd -> dd.getTaskId().equals(taskId))
            .findFirst()
            .orElseThrow(
                () ->
                    new WebApplicationException(
                        Response.status(404)
                            .entity(
                                new ErrorResponse(
                                    format(
                                        "no dispensed medications for that taskId: {0} in system",
                                        taskId)))
                            .build()));

    val kbvBundle =
        pharmacy
            .getClient()
            .getFhir()
            .decode(KbvErpBundle.class, dispenseData.getAcceptData().getKbvBundle());

    val pharmacyOrg = PharmacyOrganizationBuilder.faker().name(pharmacy.getName()).build();
    val dispensedMedication =
        DavDispensedMedicationBuilder.builder()
            .prescription(kbvBundle.getPrescriptionId())
            .pharmacy(pharmacyOrg)
            .invoice(davInvoice)
            .build();
    val davBundle =
        DavAbgabedatenBuilder.builder(kbvBundle)
            .invoice(davInvoice)
            .medication(dispensedMedication)
            .pharmacy(pharmacyOrg)
            .build();
    val davXml = pharmacy.getClient().encode(davBundle, EncodingType.XML);
    val signedDavBundle = pharmacy.signDocument(davXml);
    val chargeItem =
        ErxChargeItemBuilder.forPrescription(
                kbvBundle.getPrescriptionId()) // TaskId is always PrescriptionId
            .version(PatientenrechnungVersion.V1_0_0)
            .status("billable")
            .enterer(pharmacy.getSmcb().getTelematikId())
            .subject(kbvBundle.getKvnr(), kbvBundle.getCoverageName())
            .receiptReference(new ErxReceiptReference(dispenseData.getReceipt()))
            .verordnung(kbvBundle)
            .abgabedatensatz(davBundle.getReference(), signedDavBundle)
            .build();

    val cmd = new ChargeItemPostCommand(chargeItem, Secret.fromString(dispenseData.getSecret()));
    val response = pharmacy.erpRequest(cmd);

    val responseMap = new HashMap<String, String>();
    responseMap.put("task-id", taskId);
    responseMap.put("task-status", "closed");
    response
        .getResourceOptional(OperationOutcome.class)
        .ifPresent(
            operationOutcome ->
                responseMap.put(
                    "fd-error", OperationOutcomeWrapper.extractFrom(operationOutcome)));
    response.getResourceOptional().ifPresent(r -> responseMap.put("charge-item-id", r.getId()));
    return Response.status(response.getStatusCode()).entity(responseMap).build();
  }
}
