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

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.fhir.values.KVNR;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.val;
import org.junit.jupiter.api.Test;

class TaskGetByVsdmExamEvidenceCommandTest {

  @Test
  void requestLocatorWithEvidenceAndKvnr() {
    val examplePN = "MTIzNDU2Nw==";
    val kvnr = KVNR.from("X110499478");
    val cmd = new TaskGetByExamEvidenceCommand(examplePN).andKvnr(kvnr);

    val expected =
        format(
            "/Task?pnw={0}&kvnr={1}",
            URLEncoder.encode(examplePN, StandardCharsets.UTF_8), kvnr.getValue());
    val actual = cmd.getRequestLocator();
    assertEquals(expected, actual);
  }

  @Test
  void requestLocatorWithoutEvidence() {
    val cmd = new TaskGetByExamEvidenceCommand();
    val actual = cmd.getRequestLocator();
    assertEquals("/Task", actual);
  }

  @Test
  void ShouldBuildAdditionalQuery() {
    val cmd =
        new TaskGetByExamEvidenceCommand("")
            .andAdditionalQuery(IQueryParameter.search().withOffset(5).createParameter());
    assertNotNull(cmd);
    assertEquals("/Task?pnw=&__offset=5", cmd.getRequestLocator());
  }
}
