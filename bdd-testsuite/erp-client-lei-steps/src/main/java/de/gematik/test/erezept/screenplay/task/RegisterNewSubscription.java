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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.client.usecases.SubscriptionPostCommand;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseSubscriptionService;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Subscription;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RegisterNewSubscription implements Task {

  private String criteria;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val useSubscriptionService = SafeAbility.getAbility(actor, UseSubscriptionService.class);
    val useSMCB = SafeAbility.getAbility(actor, UseSMCB.class);

    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val subscriptionCmd =
        new SubscriptionPostCommand(
            "Communication?received=null&recipient=" + useSMCB.getTelematikID());
    try {
      val subscriptionResponse = erpClientAbility.request(subscriptionCmd);
      val subscription = subscriptionResponse.getExpectedResource();

      val subscriptionId = subscription.getIdElement().getIdPart();
      useSubscriptionService.setSubscriptionId(subscriptionId);

      val authorization =
          subscription.getChannel().getHeader().stream()
              .map(PrimitiveType::getValue)
              .filter(v -> v.startsWith("Authorization"))
              .map(v -> v.replace("Authorization: ", ""))
              .findFirst()
              .orElseThrow(
                  () -> new MissingFieldException(Subscription.class, "Bearer token is missing"));
      useSubscriptionService.setAuthorization(authorization);

    } catch (MissingFieldException | UnexpectedResponseResourceError exception) {
      log.warn(exception.getMessage());
      throw exception;
    }
  }

  public static RegisterNewSubscription forCriteria(String criteria) {
    return new RegisterNewSubscription(criteria);
  }
}
