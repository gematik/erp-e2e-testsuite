/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.cardterminal.cfg;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.cardterminal.CardTerminalClientFactory;
import de.gematik.test.cardterminal.cats.CatsClient;
import de.gematik.test.erezept.config.dto.konnektor.CardTerminalClientConfiguration;
import de.gematik.test.erezept.testutil.PrivateConstructorsUtil;
import lombok.*;
import org.junit.jupiter.api.*;

class CardTerminalClientConfigurationTest {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.throwsInvocationTargetException(CardTerminalClientFactory.class));
  }

  @Test
  void shouldCreateCatsClient() {
    val cfg = new CardTerminalClientConfiguration("kt_01", "http://localhost:80");
    val catsClient = CardTerminalClientFactory.createClient(cfg);
    assertNotNull(catsClient);
    assertEquals("kt_01", catsClient.getCtId());
  }

  @Test
  void shouldCreateCatsClientFromConfig() {
    val cfg = new CardTerminalClientConfiguration("kt_01", "http://localhost:80");
    val catsClient = new CatsClient(cfg);
    assertNotNull(catsClient);
    assertEquals("kt_01", catsClient.getCtId());
  }

  @Test
  void shouldEqualOnSameId() {
    val cfg = new CardTerminalClientConfiguration("kt_01", "http://localhost:80");
    val c1 = new CatsClient(cfg);
    val c2 = new CatsClient(cfg);
    assertEquals(c1, c2);
  }

  @Test
  void shouldNotEqualOnDifferentId() {
    val cfg = new CardTerminalClientConfiguration("kt_01", "http://localhost:80");
    val c1 = new CatsClient(cfg);
    val c2 = new CatsClient("kt_02", "http://localhost:80");
    assertNotEquals(c1, c2);
  }

  @Test
  void shouldEqualOnSameObject() {
    val cfg = new CardTerminalClientConfiguration("kt_01", "http://localhost:80");
    val c1 = new CatsClient(cfg);
    assertEquals(c1, c1);
  }

  @Test
  @SuppressWarnings("java:S3415") // (c1, null) is the intended order for this test!
  void shouldNotEqualOnNull() {
    val cfg = new CardTerminalClientConfiguration("kt_01", "http://localhost:80");
    val c1 = new CatsClient(cfg);
    assertNotEquals(c1, null);
  }

  @Test
  void shouldNotEqualOnDifferentType() {
    val cfg = new CardTerminalClientConfiguration("kt_01", "http://localhost:80");
    val c1 = new CatsClient(cfg);
    assertNotEquals("kt_01", c1);
  }
}
