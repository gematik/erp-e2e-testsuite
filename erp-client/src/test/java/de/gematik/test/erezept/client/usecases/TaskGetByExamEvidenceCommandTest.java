/*
 * Copyright (c) 2023 gematik GmbH
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.val;
import org.junit.jupiter.api.Test;

class TaskGetByExamEvidenceCommandTest {

  @Test
  void getRequestLocator() {
    val exampleKvnr = "1234567";
    val examplePN = "MTIzNDU2Nw==";
    val cmd = new TaskGetByExamEvidenceCommand(exampleKvnr, examplePN);
    val expected =
        format(
            "/Task?KVNR={0}&PNW={1}",
            URLEncoder.encode(exampleKvnr, StandardCharsets.UTF_8),
            URLEncoder.encode(examplePN, StandardCharsets.UTF_8));
    val actual = cmd.getRequestLocator();
    assertEquals(expected, actual);
  }
}
