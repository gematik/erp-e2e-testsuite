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

package de.gematik.test.erezept.screenplay.util;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import java.nio.file.Path;
import java.util.UUID;
import lombok.val;
import org.junit.Test;

public class DataMatrixCodeGeneratorTest {

  @Test
  public void createDataMatrixCode() {
    val taskId = UUID.randomUUID().toString();
    val ac = GemFaker.fakerAccessCode();

    val out = DataMatrixCodeGenerator.writeToStream(taskId, ac);
    assertNotNull(out);
    assertTrue(out.toByteArray().length > 0);
  }

  @Test
  public void writeDataMatrixCode() {
    val taskId = UUID.randomUUID().toString();
    val ac = GemFaker.fakerAccessCode();
    val filePath = Path.of("target", "dmcs", format("dmc_{0}.png", taskId));

    assertFalse(filePath.toFile().exists());
    DataMatrixCodeGenerator.writeToFile(taskId, ac, filePath.toFile());
    assertTrue(filePath.toFile().exists());
  }
}
