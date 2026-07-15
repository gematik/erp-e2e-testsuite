/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.actions;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.AuditEventGetByIdCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class DownloadAuditEventById extends ErpAction<ErxAuditEventBundle> {

  private final AuditEventGetByIdCommand cmd;

  private DownloadAuditEventById(AuditEventGetByIdCommand cmd) {
    this.cmd = cmd;
  }

  @Override
  @Step("{0} versucht ein AuditEventBundle zu einer spezifischen ID am FD abzurufen")
  public ErpInteraction<ErxAuditEventBundle> answeredBy(Actor actor) {
    val erpInteraction = this.performCommandAs(cmd, actor);

    if (erpInteraction.isOfExpectedType()) {
      log.info(
          "AuditEventById returned bundle with {} entries",
          erpInteraction.getExpectedResponse().getAuditEvents().size());
    } else {
      val resourceType =
          Objects.requireNonNull(erpInteraction.getResponse().getResourceType()).getSimpleName();

      log.info("AuditEventById returned non-bundle response of type {}", resourceType);
    }

    return erpInteraction;
  }

  public static DownloadAuditEventById forPrescriptionId(PrescriptionId prescriptionId) {
    return new DownloadAuditEventById(new AuditEventGetByIdCommand(prescriptionId));
  }
}
