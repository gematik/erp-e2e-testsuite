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

package de.gematik.test.erezept.actions;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.*;
import de.gematik.test.erezept.client.rest.param.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import java.util.List;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

@Slf4j
public class DownloadAuditEvent extends ErpAction<ErxAuditEventBundle> {

  private final AuditEventGetCommand cmd;

  private DownloadAuditEvent(AuditEventGetCommand command) {
    this.cmd = command;
  }

  @Override
  public ErpInteraction<ErxAuditEventBundle> answeredBy(Actor actor) {
    val erpInteraction = this.performCommandAs(cmd, actor);
    log.info(
        format(
            "ErxAuditEventBundle has {0} entries",
            erpInteraction.getExpectedResponse().getAuditEvents().size()));
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
