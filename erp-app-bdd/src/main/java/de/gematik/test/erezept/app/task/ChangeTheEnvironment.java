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

package de.gematik.test.erezept.app.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.Environment;
import de.gematik.test.erezept.app.mobile.elements.DebugSettings;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class ChangeTheEnvironment implements Task {

  private final Environment selectedEnvironment;

  public static ChangeTheEnvironment bySwitchInTheDebugMenuTo(Environment environment) {
    return Instrumented.instanceOf(ChangeTheEnvironment.class).withProperties(environment);
  }

  @Override
  @Step("{0} setzt die TI Umgebung auf #selectedEnvironment")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    app.logEvent(format("Change Environment to {0}", selectedEnvironment.name()));
    app.tap(Settings.DEBUG_BUTTON);

    app.tap(DebugSettings.ENVIRONMENT_SELECTOR);
    app.tap(selectedEnvironment.getPageElement());
  }
}
