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

package de.gematik.test.erezept.actions.chargeitem;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.usecases.ChargeItemPostCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.resources.dav.DavAbgabedatenBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.ChargeItem;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PostChargeItem extends ErpAction<ErxChargeItem> {

  private final PatientActor patientActor;
  private final DavAbgabedatenBundle davBundle;
  private final PrescriptionId prescriptionId;
  private final Secret secret;
  private final String kbvBundleReference;
  @Nullable private final List<NamedEnvelope<FuzzingMutator<ErxChargeItem>>> manipulator;

  public static Builder forPatient(PatientActor patient) {
    return new Builder(patient);
  }

  @Override
  @Step("{0} sendet ein ChargeItem für #patientActor zur PrescriptionId: #prescriptionId ")
  public ErpInteraction<ErxChargeItem> answeredBy(Actor actor) {
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val encodedDavBundle = erpClient.encode(davBundle, EncodingType.XML);
    val signedDavBundle = konnektor.signDocumentWithSmcb(encodedDavBundle).getPayload();
    val chargeItem =
        ErxChargeItemBuilder.forPrescription(prescriptionId)
            .status(ChargeItem.ChargeItemStatus.BILLABLE)
            .enterer(smcb.getTelematikID())
            .subject(patientActor.getKvnr(), patientActor.getInsuranceCoverage().getName())
            .verordnung(kbvBundleReference)
            .abgabedatensatz(davBundle.getReference(), signedDavBundle)
            .build();

    manipulator.forEach(
        m -> {
          log.info("manipulate ChargeItem with {}", m.getName());
          m.getParameter().accept(chargeItem);
        });

    val chargeItemPostCommand = new ChargeItemPostCommand(chargeItem, secret);
    return performCommandAs(chargeItemPostCommand, actor);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final PatientActor patient;
    private DavAbgabedatenBundle davAbgabedatenBundle;
    private final List<NamedEnvelope<FuzzingMutator<ErxChargeItem>>> manipulator =
        new LinkedList<>();

    public Builder davBundle(DavAbgabedatenBundle davAbgabedatenBundle) {
      this.davAbgabedatenBundle = davAbgabedatenBundle;
      return this;
    }

    public PostChargeItem withAcceptBundle(ErxAcceptBundle acceptBundle) {
      return withCustomValue(
          acceptBundle.getTask().getPrescriptionId(),
          acceptBundle.getSecret(),
          acceptBundle.getKbvBundleId());
    }

    public PostChargeItem withCustomValue(
        PrescriptionId prescriptionId, Secret secret, String kbvBundleReference) {
      Objects.requireNonNull(davAbgabedatenBundle, "davAbgabedatenBundle -> is missing !!!");
      return new PostChargeItem(
          patient, davAbgabedatenBundle, prescriptionId, secret, kbvBundleReference, manipulator);
    }

    public Builder withCustomStructureAndVersion(
        NamedEnvelope<FuzzingMutator<ErxChargeItem>> mutatorNamedEnvelope) {
      this.manipulator.add(mutatorNamedEnvelope);
      return this;
    }
  }
}
