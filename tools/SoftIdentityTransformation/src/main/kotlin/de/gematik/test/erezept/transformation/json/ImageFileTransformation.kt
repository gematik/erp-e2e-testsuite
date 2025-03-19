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

package de.gematik.test.erezept.transformation.json

import de.gematik.test.erezept.CertType
import de.gematik.test.erezept.CertificateElement
import de.gematik.test.erezept.transformation.CardType
import de.gematik.test.erezept.transformation.Transformation
import kotlinx.serialization.json.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.PrivateKey

data class Card(
  val iccsn: String,
  val stores: List<CertificateElement<PrivateKey>>
) {

  fun type(): String {
    return stores.firstOrNull()?.getCardType()?.name ?: "Unknown"
  }

  fun name(): String? {
    val name = stores.firstOrNull()?.getName()
    if (name.isNullOrEmpty()) {
      return null
    }
    return name
  }

  fun kvnr(): String? {
    val kvnr = stores
      .filter { it.getCardType() == CardType.EGK }
      .firstOrNull { it.getCertType() == CertType.AUT }?.getKvid();
    if (kvnr.isNullOrEmpty()) {
      return null
    }
    return kvnr
  }

}


class ImageFileTransformation(val certs: List<CertificateElement<PrivateKey>>) : Transformation {

  override fun transform(outputPath: Path) {
    val json = certs.mapToList().toJson()
    Files.writeString(
      outputPath.resolve("images.json"), json.toString(),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING,
      StandardOpenOption.WRITE
    )
  }

}

private fun Path.relativePath() = Path.of("smartcard/src/main/resources/cardimages/")
        .relativize(this).toString()


fun List<Card>.toJson(): JsonArray = buildJsonArray {
  this@toJson.filter {
    it.stores
      .any { cert -> cert.getCertType() == CertType.AUT }
  }
    .forEach { card ->
      addJsonObject {
        put("iccsn", card.iccsn)
        put("type", card.type())
        card.name()?.let {
          put("name", it)
        }
        card.kvnr()?.let {
          put("kvnr", it)
        }
        putJsonArray("stores") {
          card.stores.forEach { add(it.pathP12.relativePath()) }
        }
      }
    }
}
private fun List<CertificateElement<PrivateKey>>.mapToList(): List<Card> =
  this.groupingBy { it.getIccsn() }
    .aggregate { _: String, list: MutableList<CertificateElement<PrivateKey>>?, cert: CertificateElement<PrivateKey>, _: Boolean ->
      list?.also {
        it.add(cert)
      } ?: mutableListOf(cert)
    }.map {
      Card(it.key, it.value)
    }
