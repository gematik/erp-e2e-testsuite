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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.rest.param.QueryParameter;
import java.util.LinkedList;
import lombok.val;
import org.junit.jupiter.api.Test;

class ConsentDeleteCommandTest {

  @Test
  void requestLocatorStartsWithSlash() {
    val consentDeleteCommand = new ConsentDeleteCommand();
    val requestString = consentDeleteCommand.getRequestLocator();
    assertTrue(requestString.startsWith("/"));
  }

  @Test
  void shouldContainCategory() {
    val consentDeleteCommand = new ConsentDeleteCommand();
    assertTrue(consentDeleteCommand.getRequestLocator().contains("?category=CHARGCONS"));
    assertTrue(consentDeleteCommand.getRequestLocator().contains("/Consent"));
  }

  @Test
  void secondConstructorShouldNotContainCategory() {
    val consentDeleteCommandWithQueryFalse =
        new ConsentDeleteCommand(new LinkedList<QueryParameter>());
    assertFalse(
        consentDeleteCommandWithQueryFalse.getRequestLocator().contains("?category=CHARGCONS"));
    assertTrue(consentDeleteCommandWithQueryFalse.getRequestLocator().contains("/Consent"));
  }

  @Test
  void getRequestBodyNotNullTest() {
    val consentDeleteCommand = new ConsentDeleteCommand();
    val consentDeleteCommandWithQueryFalse =
        new ConsentDeleteCommand(new LinkedList<QueryParameter>());
    assertNotNull(consentDeleteCommand.getRequestBody());
    assertNotNull(consentDeleteCommandWithQueryFalse.getRequestBody());
  }

  @Test
  void getRequestBodyOptionalIsEmptyTest() {
    val consentDeleteCommand = new ConsentDeleteCommand();
    assertTrue(consentDeleteCommand.getRequestBody().isEmpty());
  }
}
