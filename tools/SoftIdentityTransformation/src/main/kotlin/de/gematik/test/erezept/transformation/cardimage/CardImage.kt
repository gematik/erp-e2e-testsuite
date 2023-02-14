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

import de.gematik.test.erezept.Crypto
import de.gematik.test.erezept.crypto.certificate.Oid
import de.gematik.test.erezept.transformation.CardType

sealed class CardImage(val cardType: CardType) : XmlDocument("${cardType.name.lowercase()}_template.xml") {

  enum class Attribute(val attributeId: String) {
    EF_GDO("EF.GDO"),
    ICCSN("iccsn8"),
    BODY("body"),
    POSITION_LOGICAL_END_OF_FILE("positionLogicalEndOfFile")
  }

  enum class ElcPrivateKeyAttributes(val attributeId: String) {
    PRIVATE_KEY("privateKey"),
    P("P")
    ;
  }


  enum class RsaPrivateKeyAttributes(val attributeId: String) {
    N("n"),
    E("e"),
    D("d"),
    P("p"),
    DP("dP"),
    DQ("dQ"),
    C("q")
    ;
  }

  fun getPublicCertIdentifier(certTypeOid: Oid, crypto: Crypto): String = "EF.${certTypeOid.asCertType()}.${crypto.identifier}"


  fun getPrivateKeyIdentifier(certTypeOid: Oid, crypto: Crypto): String = "PrK.${certTypeOid.type}.${crypto.identifier}"
}

fun CardType.toCardImage(): CardImage = when (this) {
  CardType.EGK -> EgkCardImage()
  CardType.HBA -> HbaCardImage()
  CardType.SMC_B -> SmcbCardImage()
}

class EgkCardImage : CardImage(CardType.EGK)
class HbaCardImage : CardImage(CardType.HBA)
class SmcbCardImage : CardImage(CardType.SMC_B)

