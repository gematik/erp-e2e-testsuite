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

package de.gematik.test.erezept.fhir.builder.erp;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.erp.ChargeItemCommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;

public class ErxComChargeItemBuilder extends ErxCommunicationBuilder<ErxComChargeItemBuilder> {

  private final ChargeItemCommunicationType type;
  protected PatientenrechnungVersion version = PatientenrechnungVersion.V1_0_0;

  protected ErxComChargeItemBuilder(ChargeItemCommunicationType type, String message) {
    super(message);
    this.type = type;
  }

  public ErxComChargeItemBuilder version(PatientenrechnungVersion version) {
    this.version = version;
    return this;
  }

  public ErxComChargeItemBuilder basedOn(TaskId taskId) {
    return basedOn(taskId.getValue());
  }

  public ErxComChargeItemBuilder basedOn(String chargeItemId) {
    this.baseOnReference =
        chargeItemId.startsWith("ChargeItem/") ? chargeItemId : "ChargeItem/" + chargeItemId;
    return this;
  }

  public ErxComChargeItemBuilder basedOn(ErxChargeItem chargeItem) {
    val idBuilder = new StringBuilder(chargeItem.getPrescriptionId().getValue());
    chargeItem.getAccessCode().ifPresent(ac -> idBuilder.append(format("?ac={0}", ac.getValue())));
    return basedOn(idBuilder.toString());
  }

  @Override
  public ErxCommunication build() {
    checkRequired();
    val com = buildCommon(type, () -> type.getType().asCanonicalType(version));
    com.setBasedOn(List.of(new Reference(this.baseOnReference)));

    return com;
  }

  private void checkRequired() {
    this.checkRequired(baseOnReference, "A ChargeItem Communication requires a ChargeItem");
    this.checkRequired(sender, "A ChargeItem Communication requires a Sender");
  }
}
