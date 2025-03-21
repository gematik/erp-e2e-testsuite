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

package de.gematik.test.erezept.app.task.ios;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SkipOnboardingOnIOSTest {

  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    val appAbility = mock(UseIOSApp.class);
    when(appAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(appAbility);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldThrowNotImplementedException() {
    val theAppUser = OnStage.theActorCalled(userName);
    val task = new SkipOnboardingOnIOS(List.of());
    assertThrows(FeatureNotImplementedException.class, () -> task.performAs(theAppUser));
  }
}
