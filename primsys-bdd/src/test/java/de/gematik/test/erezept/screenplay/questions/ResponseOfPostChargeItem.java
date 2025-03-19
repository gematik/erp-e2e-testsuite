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

package de.gematik.test.erezept.screenplay.questions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemPostCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.strategy.pharmacy.AcceptedPrescriptionStrategy;
import de.gematik.test.erezept.screenplay.strategy.pharmacy.DispensedPrescriptionStrategy;
import de.gematik.test.erezept.screenplay.strategy.pharmacy.PharmacyPrescriptionStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

public class ResponseOfPostChargeItem extends FhirResponseQuestion<ErxChargeItem> {

  private final PharmacyPrescriptionStrategy strategy;
  @Nullable private final Actor apothecary;

  private ResponseOfPostChargeItem(
      PharmacyPrescriptionStrategy strategy, @Nullable Actor apothecary) {
    this.strategy = strategy;
    this.apothecary = apothecary;
  }

  public static DispensedPrescriptionStrategy.Builder<Builder> fromDispensed(String order) {
    return fromDispensed(DequeStrategy.fromString(order));
  }

  public static DispensedPrescriptionStrategy.Builder<Builder> fromDispensed(DequeStrategy deque) {
    return new DispensedPrescriptionStrategy.ConcreteBuilder<>(deque, Builder::new);
  }

  public static AcceptedPrescriptionStrategy.Builder<Builder> fromAccepted(String order) {
    return fromAccepted(DequeStrategy.fromString(order));
  }

  public static AcceptedPrescriptionStrategy.Builder<Builder> fromAccepted(DequeStrategy deque) {
    return new AcceptedPrescriptionStrategy.ConcreteBuilder<>(deque, Builder::new);
  }

  @Override
  public ErpResponse<ErxChargeItem> answeredBy(Actor actor) {
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    strategy.init(actor);

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

    val chargeItem =
        ErxChargeItemBuilder.forPrescription(strategy.getPrescriptionId())
            .version(PatientenrechnungVersion.V1_0_0) // we will always use the new version for now!
            .status("billable")
            .enterer(smcb.getTelematikID())
            .subject(strategy.getReceiverKvnr(), GemFaker.insuranceName())
            .verordnung(strategy.getTaskId().getValue())
            .abgabedatensatz(davBundle.getReference(), signer.apply(davBundle))
            .build();

    val cmd = new ChargeItemPostCommand(chargeItem, strategy.getSecret());
    val response = erpClient.request(cmd);
    checkNoAccessCodeAndNoReceipt(response);
    return response;
  }

  private void checkNoAccessCodeAndNoReceipt(ErpResponse<ErxChargeItem> response) {
    response
        .getResourceOptional()
        .ifPresent(
            erxChargeItem -> {
              assertTrue(
                  erxChargeItem.getContained().isEmpty(),
                  "Laut Anpassung der Spec A_23704 - E-Rezept-Fachdienst – Abrechnungsinformation"
                      + " bereitstellen – darf kein AccessCode und Quittung ausgeliefert werden."
                      + " Diese Informationen könnten im 'Contained' enthalten sein.");
              assertTrue(
                  erxChargeItem.getExtension().isEmpty(),
                  "Laut Anpassung der Spec A_23704 - E-Rezept-Fachdienst – Abrechnungsinformation"
                      + " bereitstellen – darf kein AccessCode und Quittung ausgeliefert werden."
                      + " Diese Informationen könnten in einer Extension enthalten sein.");
              assertTrue(
                  erxChargeItem.getAccessCode().isEmpty(),
                  "Laut Anpassung der Spec A_23704 - E-Rezept-Fachdienst – Abrechnungsinformation"
                      + " bereitstellen – darf kein AccessCode im Identifier ausgeliefert werden");
            });
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private final PharmacyPrescriptionStrategy strategy;

    public ResponseOfPostChargeItem signedByPharmacy() {
      return new ResponseOfPostChargeItem(strategy, null);
    }

    public ResponseOfPostChargeItem signedByApothecary(Actor apothecary) {
      return new ResponseOfPostChargeItem(strategy, apothecary);
    }
  }
}
