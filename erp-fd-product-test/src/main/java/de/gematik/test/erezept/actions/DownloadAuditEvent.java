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
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.client.usecases.AuditEventGetCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class DownloadAuditEvent extends ErpAction<ErxAuditEventBundle> {

  private final AuditEventGetCommand cmd;

  private DownloadAuditEvent(AuditEventGetCommand command) {
    this.cmd = command;
  }

  @Override
  @Step("{0} versucht ein AuditEventBundle am FD abzurufen")
  public ErpInteraction<ErxAuditEventBundle> answeredBy(Actor actor) {

    val erpInteraction = this.performCommandAs(cmd, actor);

    if (erpInteraction.isOfExpectedType()) {
      val bundle = erpInteraction.getExpectedResponse();
      log.info("ErxAuditEventBundle has {} entries", bundle.getAuditEvents().size());
    } else {
      val resourceType =
          Objects.requireNonNull(erpInteraction.getResponse().getResourceType()).getSimpleName();

      log.info("AuditEvent request returned non-bundle response of type {}", resourceType);
    }

    return erpInteraction;
  }

  public static DownloadAuditEvent orderByDateDesc() {
    val cmd = new AuditEventGetCommand(new QueryParameter("_sort", "-date"));
    return new DownloadAuditEvent(cmd);
  }

  public static DownloadAuditEvent withQueryParams(IQueryParameter... queryParameter) {
    return withQueryParams(List.of(queryParameter));
  }

  public static DownloadAuditEvent withQueryParams(List<IQueryParameter> queryParameter) {
    val cmd = new AuditEventGetCommand(queryParameter);
    return new DownloadAuditEvent(cmd);
  }
}
