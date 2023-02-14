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

package de.gematik.test.erezept.transformation.cardimage

import de.gematik.test.erezept.CertificateElement
import de.gematik.test.erezept.transformation.CardType
import de.gematik.test.erezept.transformation.Transformation
import java.nio.file.Path
import java.security.PrivateKey

data class Card(val cardType: CardType, val iccsn: String, val certs: MutableList<CertificateElement<PrivateKey>>)

class CardTransformation(val certs: List<CertificateElement<PrivateKey>>) : Transformation {

  private fun List<CertificateElement<PrivateKey>>.groupingByIccsn():
          Map<String, Card> = this.groupingBy { it.getIccsn() }
          .aggregate { iccsn: String, card: Card?, cert: CertificateElement<PrivateKey>, _: Boolean ->

            if (card == null) {
              Card(cert.getCardType(), iccsn, mutableListOf(cert))
            } else {
              card.also {
                it.certs.add(cert)
              }
            }
          }

  override fun transform(outputPath: Path) {
    certs.groupingByIccsn().forEach { (iccsn, card): Map.Entry<String, Card> ->
      listOf(
        CardImageTransformation(card.cardType, iccsn, card.certs),
        CardConfigurationTransformation(
          "${card.cardType.name.lowercase()}_$iccsn.xml",
          card.cardType,
          iccsn
        )
      ).forEach { it.transform(outputPath) }
    }
  }
}
