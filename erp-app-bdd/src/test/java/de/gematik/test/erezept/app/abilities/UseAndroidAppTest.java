/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.app.abilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.cfg.AppConfiguration;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import io.appium.java_client.android.AndroidDriver;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

class UseAndroidAppTest {

  @Test
  void shouldGetStaticValues() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("Android");
    config.setUseVirtualeGK(true);
    config.setPackageName("de.gematik.erezept");
    config.setMaxWaitTimeout(10);
    config.setPollingInterval(5);

    val driver = mock(AndroidDriver.class);

    val driverAbility = new UseAndroidApp(driver, config);
    assertTrue(driverAbility.getDriverName().toLowerCase().contains("android"));
    assertNotNull(driverAbility.toString());
    assertEquals(10, driverAbility.getMaxWaitTimeout());
    assertEquals(5, driverAbility.getPollingInterval());
    assertTrue(driverAbility.useVirtualeGK());

    assertDoesNotThrow(driverAbility::tearDown);
  }

  @Test
  void shouldGetElement() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("Android");
    config.setPackageName("de.gematik.erezept");
    config.setMaxWaitTimeout(50);
    config.setPollingInterval(5);

    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any())).thenReturn(webElement);

    val driverAbility = new UseAndroidApp(driver, config);
    assertEquals(webElement, driverAbility.getWebElement(Onboarding.NEXT));
  }

  @Test
  void shouldGetElementsAsList() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("Android");
    config.setPackageName("de.gematik.erezept");
    config.setMaxWaitTimeout(50);
    config.setPollingInterval(5);

    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElements(any())).thenReturn(List.of(webElement));

    val driverAbility = new UseAndroidApp(driver, config);
    val elementList = driverAbility.getWebElementList(Onboarding.NEXT);
    assertEquals(1, elementList.size());
  }

  @Test
  void shouldGetSlowElement() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("Android");
    config.setPackageName("de.gematik.erezept");
    config.setMaxWaitTimeout(50);
    config.setPollingInterval(5);

    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any()))
        .thenThrow(new NoSuchElementException("Not found 1")) // throw on first call
        .thenThrow(new NoSuchElementException("Not found 2")) // throw on second call
        .thenReturn(webElement);

    val driverAbility = new UseAndroidApp(driver, config);
    assertEquals(webElement, driverAbility.getWebElement(Onboarding.NEXT));
  }

  @Test
  void shouldFailOnTooSlowElement() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("Android");
    config.setPackageName("de.gematik.erezept");
    config.setMaxWaitTimeout(10);
    config.setPollingInterval(5);

    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any()))
        .thenThrow(new NoSuchElementException("Not found 1")) // throw on first call
        .thenThrow(new NoSuchElementException("Not found 2")) // throw on second call
        .thenThrow(new NoSuchElementException("Not found 3")) // throw on third call
        .thenReturn(webElement);

    val driverAbility = new UseAndroidApp(driver, config);
    assertThrows(NoSuchElementException.class, () -> driverAbility.getWebElement(Onboarding.NEXT));
  }
}
