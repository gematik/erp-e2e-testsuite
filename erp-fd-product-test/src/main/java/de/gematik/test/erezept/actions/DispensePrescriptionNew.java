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

package de.gematik.test.erezept.actions;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.DispensePrescriptionCommandNew;
import de.gematik.test.erezept.fhir.r4.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DispensePrescriptionNew extends ErpAction<EmptyResource> {

  private final DispensePrescriptionCommandNew dispensePrescriptionAsBundleCommandNew;

  public static Builder withCredentials(TaskId taskId, Secret secret) {
    return new Builder(taskId, secret);
  }

  @Override
  @Step("{0} dispensiert ein E-Rezept mit #taskId und #accessCode ohne close")
  public ErpInteraction<EmptyResource> answeredBy(Actor actor) {
    return this.performCommandAs(dispensePrescriptionAsBundleCommandNew, actor);
  }

  @AllArgsConstructor
  public static class Builder {
    private final TaskId taskId;
    private final Secret secret;
    private final List<Consumer<KbvErpBundle>> manipulator = new LinkedList<>();

    public DispensePrescriptionNew withParameters(GemDispenseOperationParameters params) {
      val cmd = new DispensePrescriptionCommandNew(taskId, secret, params);
      return new DispensePrescriptionNew(cmd);
    }
  }
}
