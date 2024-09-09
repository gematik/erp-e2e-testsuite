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

package de.gematik.test.erezept.app.questions;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.PharmacyDeliveryOptions;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ThePharmacyProvidesDeliveryOptions implements Question<Boolean> {

  private final Map<String, String> options;

  @Override
  public Boolean answeredBy(Actor actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    val pharmacyName = options.getOrDefault("Name", "n/a");
    return Arrays.stream(PharmacyDeliveryOptions.values())
        .allMatch(
            pageElement -> {
              val key = pageElement.getElementKey();
              val value = options.getOrDefault(key, "false");
              val shouldSupport = Boolean.parseBoolean(value);
              val matchesExpectation = app.isPresent(pageElement) == shouldSupport;
              if (!matchesExpectation) {
                // Note: throwing AppStateMissmatchException might be a better solution
                log.error(
                    "For pharmacy {} the option {} is expected to be {} but it is not",
                    pharmacyName,
                    key,
                    value);
              }
              return matchesExpectation;
            });
  }

  public static ThePharmacyProvidesDeliveryOptions givenFrom(List<Map<String, String>> options) {
    // we are only interested in the first row here!
    return givenFrom(options.get(0));
  }

  public static ThePharmacyProvidesDeliveryOptions givenFrom(Map<String, String> options) {
    return new ThePharmacyProvidesDeliveryOptions(options);
  }
}
