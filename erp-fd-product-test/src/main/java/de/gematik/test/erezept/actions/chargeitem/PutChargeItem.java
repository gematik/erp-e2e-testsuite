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

package de.gematik.test.erezept.actions.chargeitem;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.ChargeItemPutCommand;
import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvAbgabedatenBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PutChargeItem extends ErpAction<ErxChargeItem> {

  private final DavPkvAbgabedatenBundle davBundle;
  private final AccessCode ac;
  private final List<NamedEnvelope<FuzzingMutator<ErxChargeItem>>> manipulator;

  private final ErxChargeItem referencedChargeItem;

  public static Builder withPatientsAccessCode(AccessCode accessCode) {
    return new Builder(accessCode);
  }

  @Override
  @Step("{0} sendet ein neues ChargeItem für #patientActor zur PrescriptionId: #prescriptionId ")
  public ErpInteraction<ErxChargeItem> answeredBy(Actor actor) {
    val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val davXml = erpClient.encode(davBundle, EncodingType.XML);
    val signedDavBundle = konnektor.signDocumentWithSmcb(davXml).getPayload();
    val changedChargeItem =
        referencedChargeItem.withChangedContainedBinaryData(
            davBundle.getReference(), signedDavBundle);

    manipulator.forEach(
        m -> {
          log.info("manipulate ChargeItem with {}", m.getName());
          m.getParameter().accept(changedChargeItem);
        });

    val cmd = new ChargeItemPutCommand(ac, changedChargeItem);

    return performCommandAs(cmd, actor);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final AccessCode accessCode;
    private DavPkvAbgabedatenBundle davAbgabedatenBundle;
    private final List<NamedEnvelope<FuzzingMutator<ErxChargeItem>>> manipulator =
        new LinkedList<>();

    public Builder davBundle(DavPkvAbgabedatenBundle davAbgabedatenBundle) {
      this.davAbgabedatenBundle = davAbgabedatenBundle;
      return this;
    }

    public PutChargeItem andReferencedChargeItem(ErxChargeItem referencedChargeItem) {
      if (davAbgabedatenBundle == null) {
        log.warn("davAbgabedatenBundle -> is missing !!! it has been replaced by a fake value");
        davAbgabedatenBundle =
            DavPkvAbgabedatenFaker.builder(referencedChargeItem.getPrescriptionId()).fake();
      }
      return new PutChargeItem(davAbgabedatenBundle, accessCode, manipulator, referencedChargeItem);
    }

    public Builder withMutators(
        List<NamedEnvelope<FuzzingMutator<ErxChargeItem>>> mutatorNamedEnvelope) {
      this.manipulator.addAll(mutatorNamedEnvelope);
      return this;
    }
  }
}
