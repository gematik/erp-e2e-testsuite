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

package de.gematik.test.erezept.transformation.json

import de.gematik.test.erezept.getCertificateElements
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Path

internal class ImageFileTransformationTest {

  private fun JsonElement.value(): String {
    return this.toString().replace("\"", "")
  }

  @Test
  fun `ensure that generated json contains elements and values`() {
    val cert = Path.of("src/test/resources/80276883110000113311-C_CH_AUT_R2048.p12")
      .getCertificateElements().first()
    assertNotNull(cert)

    val card = Card(cert.getIccsn(), listOf(cert))

    val json = listOf(card).toJson()

    assertNotNull(json)
    assertEquals(1, json.size)
    val element = json[0].jsonObject
    assertNotNull(element)
    assertTrue(element.containsKey("iccsn"))
    assertTrue(element.containsKey("type"))
    assertTrue(element.containsKey("name"))
    assertTrue(element.containsKey("keys"))

    assertEquals("80276883110000113311", element["iccsn"]?.value())
    assertEquals("EGK", element["type"]?.value())
    assertEquals("Schraßer, Fridolin Fedilio Diether Johann Wolfgang", element["name"]?.value())
    assertEquals("X110406067", element["kvnr"]?.value())

    val keys = element["keys"]?.jsonArray
    assertNotNull(keys)
    assertEquals(
      "../../../../../src/test/resources/80276883110000113311-C_CH_AUT_R2048.p12",
      keys!![0].value()
    )

  }
}