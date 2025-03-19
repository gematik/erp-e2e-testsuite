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

package de.gematik.test.erezept.transformation.cardimage

import de.gematik.test.erezept.Crypto
import de.gematik.test.erezept.crypto.certificate.Oid
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardImageTest {
  private lateinit var cardImage: EgkCardImage

  @BeforeEach
  fun setup() {
    cardImage = EgkCardImage()
  }

  @Test
  fun `Should correctly identify RSA public key attributes`() {
    cardImage.element(cardImage.getPublicCertIdentifier(Oid.OID_EGK_AUT, Crypto.RSA))?.let { element ->
      assertNotNull(element.attribute(CardImage.Attribute.BODY.attributeId))
      assertNotNull(element.attribute(CardImage.Attribute.POSITION_LOGICAL_END_OF_FILE.attributeId))
    }
  }

  @Test
  fun `Should correctly identify RSA private key attributes`() {
    cardImage.element(cardImage.getPrivateKeyIdentifier(Oid.OID_EGK_AUT, Crypto.RSA))?.let { element ->
      CardImage.RsaPrivateKeyAttributes.values().forEach {
        assertNotNull(element.attribute(it.attributeId))
      }
    }
  }

  @Test
  fun `Should correctly identify ECC public key attributes`() {
    cardImage.element(cardImage.getPublicCertIdentifier(Oid.OID_EGK_AUT, Crypto.ECC))?.let { element ->
      assertNotNull(element.attribute(CardImage.Attribute.BODY.attributeId))
      assertNotNull(element.attribute(CardImage.Attribute.POSITION_LOGICAL_END_OF_FILE.attributeId))
    }
  }

  @Test
  fun `Should correctly identify ECC private key attributes`() {
    cardImage.element(cardImage.getPrivateKeyIdentifier(Oid.OID_EGK_AUT, Crypto.ECC))?.let { element ->
      CardImage.ElcPrivateKeyAttributes.values().forEach {
        assertNotNull(element.attribute(it.attributeId))
      }
    }
  }
}
