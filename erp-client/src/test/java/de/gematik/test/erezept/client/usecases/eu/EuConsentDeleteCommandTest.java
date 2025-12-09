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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.client.usecases.eu;

import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;

class EuConsentDeleteCommandTest {
  @Test
  void requestLocatorStartsWithSlash() {
    val consentDeleteCommand = new EuConsentDeleteCommand();
    val requestString = consentDeleteCommand.getRequestLocator();
    assertTrue(requestString.startsWith("/"));
  }

  @Test
  void shouldContainCategory() {
    val consentDeleteCommand = new EuConsentDeleteCommand();
    assertTrue(consentDeleteCommand.getRequestLocator().contains("?category=EUDISPCONS"));
    assertTrue(consentDeleteCommand.getRequestLocator().contains("/Consent"));
  }

  @Test
  void getRequestBodyOptionalIsEmptyTest() {
    val consentDeleteCommand = new EuConsentDeleteCommand();
    assertTrue(consentDeleteCommand.getRequestBody().isEmpty());
  }
}
