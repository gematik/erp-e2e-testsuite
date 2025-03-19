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

package de.gematik.test.erezept.app.abilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import de.gematik.test.erezept.config.dto.app.AppiumConfiguration;
import io.appium.java_client.android.AndroidDriver;
import io.cucumber.java.Scenario;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

class UseAndroidAppTest {

  @Test
  void shouldGetStaticValues() {
    val appiumConfig = new AppiumConfiguration();
    appiumConfig.setMaxWaitTimeout(10); // poll 10 times at maximum
    appiumConfig.setPollingInterval(5);

    val driver = mock(AndroidDriver.class);

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    assertTrue(driverAbility.getDriverName().toLowerCase().contains("android"));
    assertNotNull(driverAbility.toString());
    assertEquals(10, driverAbility.getMaxWaitTimeout());
    assertEquals(5, driverAbility.getPollingInterval());

    assertDoesNotThrow(() -> driverAbility.finish(mock(Scenario.class)));
  }

  @Test
  void shouldGetElement() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any())).thenReturn(webElement);

    val driverAbility = new UseAndroidApp(driver, new AppiumConfiguration());
    assertEquals(webElement, driverAbility.getWebElement(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldGetElementsAsList() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElements(any())).thenReturn(List.of(webElement));

    val driverAbility = new UseAndroidApp(driver, new AppiumConfiguration());
    val elementList = driverAbility.getWebElements(Onboarding.NEXT_BUTTON);
    assertEquals(1, elementList.size());
  }

  @Test
  void shouldGetSlowElement() {
    val appiumConfig = new AppiumConfiguration();
    appiumConfig.setMaxWaitTimeout(50); // poll 10 times at maximum
    appiumConfig.setPollingInterval(5);

    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any()))
        .thenThrow(new NoSuchElementException("Not found 1")) // throw on first call
        .thenThrow(new NoSuchElementException("Not found 2")) // throw on second call
        .thenReturn(webElement);

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    assertEquals(webElement, driverAbility.getWebElement(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldFailOnTooSlowElement() {
    val appiumConfig = new AppiumConfiguration();
    appiumConfig.setMaxWaitTimeout(10);
    appiumConfig.setPollingInterval(5);

    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any()))
        .thenThrow(new NoSuchElementException("Not found 1")) // throw on first call
        .thenThrow(new NoSuchElementException("Not found 2")) // throw on second call
        .thenThrow(new NoSuchElementException("Not found 3")) // throw on third call
        .thenReturn(webElement);

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    assertThrows(
        NoSuchElementException.class, () -> driverAbility.getWebElement(Onboarding.NEXT_BUTTON));
  }
}
