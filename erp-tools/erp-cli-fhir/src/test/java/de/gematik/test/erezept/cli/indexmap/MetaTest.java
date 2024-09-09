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

package de.gematik.test.erezept.cli.indexmap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.Test;

class MetaTest {

  @Test
  void shouldSerializeToJson() {
    val meta = new Meta();
    meta.setMetaJsonVersion(MetaVersion.V1_0_0);
    meta.setAuthor("Gematik");
    meta.setSource("https://github.com/gematik/eRezept-Examples");
    meta.setLastUpdated(Instant.now());

    val mapper = new ObjectMapper();
    assertDoesNotThrow(() -> mapper.writeValueAsString(meta));
  }

  @Test
  void shouldCreateDefaultMeta() {
    val meta = Meta.createMeta("DAV-ABDA");
    val mapper = new ObjectMapper();
    assertDoesNotThrow(() -> mapper.writeValueAsString(meta));
  }

  @Test
  void shouldDeserializeFromJson() throws JsonProcessingException {
    val input =
        "{\"metaJsonVersion\":\"1.0.0\",\"lastUpdated\":\"2022-12-05T11:19:51.876273Z\",\"author\":\"Gematik\",\"source\":\"https://github.com/gematik/eRezept-Examples\"}";

    val mapper = new ObjectMapper();
    val meta = mapper.readValue(input, Meta.class);
    assertEquals("https://github.com/gematik/eRezept-Examples", meta.getSource());
    assertEquals("Gematik", meta.getAuthor());
    assertEquals(MetaVersion.V1_0_0.getVersion(), meta.getMetaJsonVersion().getVersion());
  }
}
