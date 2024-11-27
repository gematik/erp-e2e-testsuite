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

package de.gematik.test.erezept.client.usecases;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.builder.erp.SubscriptionBuilder;
import java.util.Arrays;
import java.util.Optional;
import lombok.NonNull;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Subscription;

public class SubscriptionPostCommand extends BaseCommand<Subscription> {

  public enum CRITERIA {
    COMMUNICATION;

    public static CRITERIA fromString(@NonNull String criteria) {
      return Arrays.stream(CRITERIA.values())
          .filter(c -> c.name().equalsIgnoreCase(criteria))
          .findFirst()
          .orElseThrow();
    }
  }

  private String criteria;

  public SubscriptionPostCommand(final String criteria) {
    super(Subscription.class, HttpRequestMethod.POST, "Subscription");
    this.criteria = criteria;
  }

  @Override
  public String getRequestLocator() {
    return this.getResourcePath();
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.of(SubscriptionBuilder.forCriteria(this.criteria).build());
  }
}
