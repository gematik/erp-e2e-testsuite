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

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.r4.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.val;
import org.junit.jupiter.api.Test;

class DispensePrescriptionCommandNewTest extends ErpFhirBuildingTest {

  @Test
  void shouldCreateCommandWithDispenseParametersAsBody() {
    val taskId = TaskId.from("123456789");
    val secret = Secret.from("anotherSecret");

    val params = new GemDispenseOperationParameters();

    val command = new DispensePrescriptionCommandNew(taskId, secret, params);

    val body = command.getRequestBody();

    assertTrue(body.isPresent());
    assertSame(params, body.get());
  }

  @Test
  void shouldYieldEmptyBodyWhenNoParametersProvided() {
    val taskId = TaskId.from("987654321");
    val secret = Secret.from("topSecret");

    val command = new DispensePrescriptionCommandNew(taskId, secret, null);

    val body = command.getRequestBody();

    assertTrue(body.isEmpty());
  }

  @Test
  void shouldBuildRequestLocatorWithDispenseOperationAndSecretQuery() {
    val taskId = TaskId.from("4711");
    val secret = Secret.from("s3cr3t");
    val params = new GemDispenseOperationParameters();

    val command = new DispensePrescriptionCommandNew(taskId, secret, params);

    val locator = command.getRequestLocator();

    assertTrue(locator.startsWith("/Task/" + taskId.getValue()));
    assertTrue(locator.contains("/$dispense"));
    assertTrue(locator.contains("?"));
    assertTrue(locator.contains("secret=" + secret.getValue()));
  }

  @Test
  void shouldExposeHttpMethodAndExpectedResponseType() {
    val taskId = TaskId.from("11");
    val secret = Secret.from("22");
    val params = new GemDispenseOperationParameters();

    val command = new DispensePrescriptionCommandNew(taskId, secret, params);

    assertEquals(HttpRequestMethod.POST, command.getMethod());
    assertEquals(EmptyResource.class, command.expectedResponseBody());
  }
}
