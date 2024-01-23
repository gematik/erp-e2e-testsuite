/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.app.mocker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;
import org.openqa.selenium.WebElement;

public class WebElementMockFactory {

  public static WebElement createRedeemablePrescription() {
    return createPrescription("Einlösbar");
  }

  public static WebElement createPrescription(String label) {
    val p = mock(WebElement.class);
    when(p.getText()).thenReturn(label);
    return p;
  }

  public static WebElement getDisplayableMockElement(boolean isDisplayed, boolean isEnabled) {
    val mockElement = mock(WebElement.class);
    when(mockElement.isDisplayed()).thenReturn(isDisplayed);
    when(mockElement.isEnabled()).thenReturn(isEnabled);
    return mockElement;
  }
}
