/*
 * Copyright 2023 gematik GmbH
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

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class GetPrescriptionById extends ErpAction<ErxPrescriptionBundle> {
  private final TaskId taskId;
  @Nullable private final AccessCode accessCode;
  @Nullable private final Secret secret;

  @Override
  public ErpInteraction<ErxPrescriptionBundle> answeredBy(Actor actor) {

    TaskGetByIdCommand cmd;
    if (accessCode != null) {
      cmd = new TaskGetByIdCommand(taskId, accessCode);
    } else if (secret != null) {
      cmd = new TaskGetByIdCommand(taskId, secret);
    } else {
      cmd = new TaskGetByIdCommand(taskId);
    }
    return this.performCommandAs(cmd, actor);
  }

  public static Builder withTaskId(TaskId taskId) {
    return new Builder(taskId);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final TaskId taskId;

    public GetPrescriptionById withoutAuthentication() {
      return new GetPrescriptionById(taskId, null, null);
    }

    public GetPrescriptionById withAccessCode(AccessCode accessCode) {
      return new GetPrescriptionById(taskId, accessCode, null);
    }

    public GetPrescriptionById withSecret(Secret secret) {
      return new GetPrescriptionById(taskId, null, secret);
    }
  }
}
