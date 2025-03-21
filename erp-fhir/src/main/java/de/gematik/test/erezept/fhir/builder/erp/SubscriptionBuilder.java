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
 */

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.hl7.fhir.r4.model.Subscription;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelComponent;
import org.hl7.fhir.r4.model.Subscription.SubscriptionChannelType;
import org.hl7.fhir.r4.model.Subscription.SubscriptionStatus;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Setter
public class SubscriptionBuilder extends ResourceBuilder<Subscription, SubscriptionBuilder> {

  private final String criteria;

  public static SubscriptionBuilder forCriteria(String criteria) {
    return new SubscriptionBuilder(criteria);
  }

  @Override
  public Subscription build() {
    val subscription = new Subscription();
    subscription.setStatus(SubscriptionStatus.REQUESTED);
    subscription.setReason("none");
    subscription.setCriteria(this.criteria);

    val scc = new SubscriptionChannelComponent();
    scc.setType(SubscriptionChannelType.WEBSOCKET);
    subscription.setChannel(scc);

    return subscription;
  }
}
