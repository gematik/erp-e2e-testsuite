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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.dav.*;
import de.gematik.test.erezept.fhir.builder.erp.*;
import de.gematik.test.erezept.fhir.parser.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.resources.dav.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.util.function.*;
import javax.annotation.*;
import lombok.*;
import net.serenitybdd.screenplay.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseOfPostChargeItem extends FhirResponseQuestion<ErxChargeItem> {

  private final DequeStrategy deque;
  @Nullable private final Actor apothecary;

  public static Builder fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static Builder fromStack(DequeStrategy deque) {
    return new Builder(deque);
  }

  @Override
  public Class<ErxChargeItem> expectedResponseBody() {
    return ErxChargeItem.class;
  }

  @Override
  public String getOperationName() {
    return "POST /ChargeItem";
  }

  @Override
  public ErpResponse answeredBy(Actor actor) {
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val prescriptionStack = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val dispensed = deque.chooseFrom(prescriptionStack.getDispensedPrescriptions());

    // use a random faked DavBundle for now
    val davBundle = DavAbgabedatenBuilder.faker(dispensed.getPrescriptionId()).build();
    Function<DavAbgabedatenBundle, byte[]> signer;
    if (apothecary != null) {
      // sign as apothecary with HBA
      val konnektor = SafeAbility.getAbility(apothecary, UseTheKonnektor.class);
      signer =
          (b) -> {
            val encoded = erpClient.encode(b, EncodingType.XML);
            return konnektor.signDocumentWithHba(encoded).getPayload();
          };
    } else {
      // sign as pharmacy with SMC-B
      val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);
      signer =
          (b) -> {
            val encoded = erpClient.encode(b, EncodingType.XML);
            return konnektor.signDocumentWithSmcb(encoded).getPayload();
          };
    }

    val chargeItem =
        ErxChargeItemBuilder.forPrescription(dispensed.getPrescriptionId())
            .version(PatientenrechnungVersion.V1_0_0) // we will always use the new version for now!
            .accessCode(dispensed.getAccessCode())
            .status("billable")
            .enterer(smcb.getTelematikID())
            .subject(dispensed.getReceiverKvid(), GemFaker.insuranceName())
            .markingFlag(false, false, true)
            .verordnung(dispensed.getTaskId())
            .abgabedatensatz(davBundle, signer)
            .build();

    val cmd = new ChargeItemPostCommand(chargeItem, dispensed.getSecret());
    return erpClient.request(cmd);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy deque;

    public ResponseOfPostChargeItem signedByPharmacy() {
      return new ResponseOfPostChargeItem(deque, null);
    }

    public ResponseOfPostChargeItem signedByApothecary(Actor apothecary) {
      return new ResponseOfPostChargeItem(deque, apothecary);
    }
  }
}
