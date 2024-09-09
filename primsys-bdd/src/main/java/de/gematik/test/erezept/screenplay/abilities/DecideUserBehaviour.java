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

package de.gematik.test.erezept.screenplay.abilities;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.screenplay.Ability;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DecideUserBehaviour implements Ability {

  private final boolean preferManualSteps;

  public boolean doesPreferManualSteps() {
    return this.preferManualSteps;
  }

  public static DecideUserBehaviour automated() {
    return withGiven(false);
  }

  public static DecideUserBehaviour manual() {
    return withGiven(true);
  }

  public static DecideUserBehaviour withGiven(boolean choice) {
    return new DecideUserBehaviour(choice);
  }

  @Override
  public String toString() {
    return (preferManualSteps)
        ? "Benutzer verwendet manuelle Schritte wenn möglich"
        : "Benutzer Interaktionen sind automatisiert";
  }
}
