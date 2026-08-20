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

package de.gematik.test.erezept.actions.subscription;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.SubscriptionPostCommand;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Subscription;

@RequiredArgsConstructor
public class PostSubscription extends ErpAction<Subscription> {

  private final String criteria;

  @Override
  public ErpInteraction<Subscription> answeredBy(Actor actor) {
    return performCommandAs(new SubscriptionPostCommand(criteria), actor);
  }

  public static PostSubscription withCriteria(String criteria) {
    return new PostSubscription(criteria);
  }
}
