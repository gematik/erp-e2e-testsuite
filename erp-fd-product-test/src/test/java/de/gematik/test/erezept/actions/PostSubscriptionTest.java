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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.SubscriptionPostCommand;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Subscription;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PostSubscriptionTest extends ErpFhirParsingTest {

  @Test
  void shouldPerformSubscriptionPostCommand() {

    Actor actor = Mockito.mock(Actor.class);

    @SuppressWarnings("unchecked")
    ErpInteraction<Subscription> expectedInteraction = Mockito.mock(ErpInteraction.class);

    PostSubscription action = spy(PostSubscription.withCriteria("communication"));

    doReturn(expectedInteraction)
        .when(action)
        .performCommandAs(any(SubscriptionPostCommand.class), Mockito.eq(actor));

    ErpInteraction<Subscription> result = action.answeredBy(actor);

    assertSame(expectedInteraction, result);

    verify(action).performCommandAs(any(SubscriptionPostCommand.class), Mockito.eq(actor));
  }
}
