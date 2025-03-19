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
 */

package de.gematik.test.erezept.client.usecases.search;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.rest.param.QueryParameter;
import lombok.val;
import org.junit.jupiter.api.Test;

class ConsentDeleteBuilderTest {

  @Test
  void shouldGenerateBuilderCorrect() {
    val consentDeleteB = ConsentDeleteBuilder.withValidParams();
    assertTrue(consentDeleteB.getRequestLocator().contains("?category=CHARGCONS"));
  }

  @Test
  void shouldBuildWithCustomCategory() {
    val consentDeleteB = ConsentDeleteBuilder.withCustomCategory("Test");
    assertTrue(consentDeleteB.getRequestLocator().contains("?category=Test"));
  }

  @Test
  void shouldBuildWithCustomQuerySet() {
    val consentDeleteB =
        ConsentDeleteBuilder.withCustomQuerySet()
            .addQuery(new QueryParameter("TestKey", "TestValue"))
            .build();

    assertTrue(consentDeleteB.getRequestLocator().contains("?TestKey=TestValue"));
  }

  @Test
  void ShouldGenerateBody() {
    val consentDeleteB = ConsentDeleteBuilder.withValidParams();
    assertNotNull(consentDeleteB.getRequestBody());
  }
}
