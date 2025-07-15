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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemPutCommand;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.strategy.pharmacy.AuthorizedChargeItemStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Resource;

public class ResponseOfPutChargeItem extends FhirResponseQuestion<ErxChargeItem> {

  private final AuthorizedChargeItemStrategy strategy;
  @Nullable private final Actor apothecary;

  private ResponseOfPutChargeItem(
      AuthorizedChargeItemStrategy strategy, @Nullable Actor apothecary) {
    this.strategy = strategy;
    this.apothecary = apothecary;
  }

  @Override
  public ErpResponse<ErxChargeItem> answeredBy(Actor actor) {
    strategy.init(actor);

    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    // decide who should sign the DAV-Bundle
    val smartcardType =
        Optional.ofNullable(this.apothecary)
            .map(a -> SmartcardType.HBA)
            .orElse(SmartcardType.SMC_B);
    val konnektorOwner = Optional.ofNullable(this.apothecary).orElse(actor);

    // use a random faked DavBundle for now
    val davBundle = DavPkvAbgabedatenFaker.builder(strategy.getPrescriptionId()).fake();
    val signer =
        konnektorOwner.asksFor(
            TheFhirDocumentSigner.withEncoder(b -> erpClient.encode(davBundle, EncodingType.XML))
                .using(smartcardType));

    // get the original ChargeItem first
    val theChargeItem = strategy.getChargeItem();
    if (theChargeItem.getAccessCode().isEmpty()) {
      theChargeItem.addIdentifier(
          strategy.getAccessCode().asIdentifier(ErpWorkflowNamingSystem.ACCESS_CODE));
    }

    // remove the old contained resource and the supporting information reference to it
    theChargeItem.getContained().clear();
    theChargeItem
        .getSupportingInformation()
        .removeIf(
            si ->
                AbdaErpPkvStructDef.PKV_ABGABEDATENSATZ.matches(si.getDisplay())
                    || si.getDisplay().equals("Binary"));

    // create a new contained resource
    val containedDavBundle =
        new Binary().setContentType("application/pkcs7-mime").setContent(signer.apply(davBundle));
    val davReference = davBundle.getReference();
    containedDavBundle.setId(davReference.getReference(false));
    theChargeItem.addContained((Resource) containedDavBundle);

    // manipulate the reference to be valid for newer profiles
    davReference.makeContained();
    davReference.setDisplay("Binary");
    davReference.setType(null);
    theChargeItem.addSupportingInformation(davReference);

    // PUT changes on Server
    val cmd =
        new ChargeItemPutCommand(
            strategy.getPrescriptionId(), strategy.getAccessCode(), theChargeItem);
    return erpClient.request(cmd);
  }

  public static AuthorizedChargeItemStrategy.ConcreteBuilder<ResponseOfPutChargeItem.Builder>
      fromDispensed(String order) {
    return fromDispensed(DequeStrategy.fromString(order));
  }

  public static AuthorizedChargeItemStrategy.ConcreteBuilder<ResponseOfPutChargeItem.Builder>
      fromDispensed(DequeStrategy deque) {
    return new AuthorizedChargeItemStrategy.ConcreteBuilder<>(deque, Builder::new);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private final AuthorizedChargeItemStrategy strategy;

    public ResponseOfPutChargeItem signedByPharmacy() {
      return new ResponseOfPutChargeItem(strategy, null);
    }

    public ResponseOfPutChargeItem signedByApothecary(Actor apothecary) {
      return new ResponseOfPutChargeItem(strategy, apothecary);
    }
  }
}
