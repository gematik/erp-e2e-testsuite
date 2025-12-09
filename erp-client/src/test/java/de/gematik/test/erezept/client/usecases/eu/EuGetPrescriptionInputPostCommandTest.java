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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.fhir.r4.eu.EuGetPrescriptionInput;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuGetPrescriptionInputPostCommandTest {

  private final EuGetPrescriptionPostCommand euGetPrescriptionPostCommand =
      new EuGetPrescriptionPostCommand(mock(EuGetPrescriptionInput.class));

  @Test
  void getRequestLocatorFirstPartIsGetEuPrescription() {
    val request = euGetPrescriptionPostCommand.getRequestLocator();
    val requestStringArray = request.split("/");
    assertEquals("$get-eu-prescriptions", requestStringArray[1]);
  }

  @Test
  void shouldSetCountAsPathParamCorrect() {
    val request =
        new EuGetPrescriptionPostCommand(
                mock(EuGetPrescriptionInput.class),
                IQueryParameter.search().withCount(4).createParameter())
            .getRequestLocator();
    assertTrue(request.contains("?_count=4"));
  }

  @Test
  void euPrescriptionInputCommandShouldHavaCorrectPath() {
    val request =
        EuGetPrescriptionPostCommand.forDemographics(mock(EuGetPrescriptionInput.class))
            .getRequestLocator();
    val requestStringArray = request.split("\\?");
    assertEquals("$get-eu-prescriptions", requestStringArray[0].split("/")[1]);
    assertTrue(request.contains("?_count=1"));
  }

  @Test
  void getRequestBodyShouldBeNotNull() {
    val request = euGetPrescriptionPostCommand.getRequestBody();
    assertNotNull(request);
  }

  @Test
  void getRequestBodyShouldBeIsOptionalPresent() {
    val request = euGetPrescriptionPostCommand.getRequestBody();
    assertTrue(request.isPresent());
  }

  @Test
  void getRequestLocatorResponseShouldStartsWithSlash() {
    val requestString = euGetPrescriptionPostCommand.getRequestLocator();
    assertTrue(requestString.startsWith("/"));
  }

  @Test
  void forEuPrescriptionsShouldCreateCommandWithInputOnly() {
    EuGetPrescriptionInput input = mock(EuGetPrescriptionInput.class);
    EuGetPrescriptionPostCommand command = EuGetPrescriptionPostCommand.forEuPrescriptions(input);

    assertNotNull(command);
    assertTrue(command.getRequestBody().isPresent());
    assertEquals(input, command.getRequestBody().get());
    assertEquals("$get-eu-prescriptions", command.getRequestLocator().split("/")[1]);
  }

  @Test
  void forEuPrescriptionsShouldCreateCommandWithInputAndCount() {
    EuGetPrescriptionInput input = mock(EuGetPrescriptionInput.class);
    int count = 5;
    EuGetPrescriptionPostCommand command =
        EuGetPrescriptionPostCommand.forEuPrescriptions(input, count);

    assertNotNull(command);
    assertTrue(command.getRequestBody().isPresent());
    assertEquals(input, command.getRequestBody().get());
    assertTrue(command.getRequestLocator().contains("?_count=" + count));
  }
}
