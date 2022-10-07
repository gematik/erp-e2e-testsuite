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

package de.gematik.test.erezept.client.usecases;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import lombok.val;
import org.junit.jupiter.api.Test;

class TaskGetByIdCommandTest {

  @Test
  void getRequestLocator() {
    val taskId = "123456789";
    val cmd = new TaskGetByIdCommand(taskId);

    val expected = "/Task/" + taskId;
    val actual = cmd.getRequestLocator();
    assertEquals(expected, actual);
  }

  @Test
  void requestBodyShouldBeNotNull() {
    val taskId = PrescriptionId.random().getValue();
    val taskGetByIdCommand = new TaskGetByIdCommand(taskId);
    assertNotNull(taskGetByIdCommand.getRequestBody());
  }

  @Test
  void shouldContainAccessCodeOnlyInHeader() {
    val taskId = PrescriptionId.random().getValue();
    val accessCode = AccessCode.random();
    val cmd = new TaskGetByIdCommand(taskId, accessCode);

    val expected = format("/Task/{0}", taskId);
    assertEquals(expected, cmd.getRequestLocator());

    val headers = cmd.getHeaderParameters();
    assertEquals(1, headers.size());
    val xAccessCode = headers.get("X-AccessCode");
    assertEquals(accessCode.getValue(), xAccessCode);
  }

  @Test
  void shouldContainSecret() {
    val taskId = PrescriptionId.random().getValue();
    val secret = new Secret("abc");
    val cmd = new TaskGetByIdCommand(taskId, secret);

    val expected = format("/Task/{0}?secret={1}", taskId, secret.getValue());
    assertEquals(expected, cmd.getRequestLocator());
  }
}
