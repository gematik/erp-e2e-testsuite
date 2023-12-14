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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.screenplay.abilities.UseSubscriptionService;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HasNewSubscriptionPing implements Question<Boolean> {

  @Override
  public Boolean answeredBy(Actor actor) {
    val useSubscriptionService = SafeAbility.getAbility(actor, UseSubscriptionService.class);
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return useSubscriptionService.getWebsocket().hasNewPing();
  }

  public static HasNewSubscriptionPing hasNewPing() {
    return new HasNewSubscriptionPing();
  }
}
