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

package de.gematik.test.erezept.eml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class PutConfigurationTest {

  @Test
  void testConstructor() {
    val identifier = RuleIdentifier.PROVIDE_PRESCRIPTION;
    val rule = RuleDto.reply("123456789", RuleTemplate.PROVIDE_PRESCRIPTION_SUCCESS);

    val putConfiguration = new PutConfiguration(identifier, rule);

    assertEquals(identifier, putConfiguration.getIdentifier());
    assertEquals(rule, putConfiguration.getRule());
  }

  @SneakyThrows
  @Test
  void shouldPutConfiguration() {
    val identifier = RuleIdentifier.PROVIDE_PRESCRIPTION;
    val rule = RuleDto.reply("123456789", RuleTemplate.PROVIDE_PRESCRIPTION_SUCCESS);

    val putConfiguration = new PutConfiguration(identifier, rule);
    val httpBRequest = putConfiguration.getHttpBRequest();

    assertNotNull(httpBRequest);
    assertEquals(HttpRequestMethod.PUT, httpBRequest.method());
    assertTrue(
        httpBRequest.urlPath().contains(identifier.getUrlname()),
        "The URL path should contain the expected identifier");

    val mapper = new ObjectMapper();
    val payload = mapper.writeValueAsString(rule);

    assertEquals(new String(httpBRequest.body(), StandardCharsets.UTF_8), payload);
  }
}
