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

package de.gematik.test.erezept.transformation.cardimage

import de.gematik.test.erezept.CertificateElement
import de.gematik.test.erezept.Crypto
import de.gematik.test.erezept.crypto.certificate.Oid
import de.gematik.test.erezept.crypto.certificate.X509CertificateWrapper
import de.gematik.test.erezept.transformation.CardType
import de.gematik.test.erezept.transformation.Transformation
import de.gematik.test.erezept.transformation.cardimage.CardImage.Attribute
import de.gematik.test.erezept.transformation.cardimage.CardImage.ElcPrivateKeyAttributes.PRIVATE_KEY
import de.gematik.test.erezept.transformation.cardimage.CardImage.RsaPrivateKeyAttributes.*
import org.bouncycastle.util.encoders.Hex
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.RSAPrivateKey
import java.security.spec.RSAPrivateCrtKeySpec
import de.gematik.test.erezept.transformation.cardimage.CardImage.ElcPrivateKeyAttributes.P as P_ECC
import de.gematik.test.erezept.transformation.cardimage.CardImage.RsaPrivateKeyAttributes.P as P_RSA
class CardImageTransformation(
        cardType: CardType,
        private val iccsn: String,
        private val certElements: List<CertificateElement<PrivateKey>>,
        private val outputFilename: String = "${cardType.name.lowercase()}_$iccsn.xml",
) : Transformation {

  private val cardImage: CardImage = cardType.toCardImage()

  private val log = LoggerFactory.getLogger(javaClass)

  private fun ByteArray.asHex(): String {
    return String(Hex.encode(this)).uppercase()
  }

  private fun BigInteger.asHex(): String {
    return this.toString(16).uppercase()
  }

  private fun PrivateKey.asRsaPrivateCrtKey(): RSAPrivateCrtKeySpec? {
    val keyFac: KeyFactory = KeyFactory.getInstance(this.algorithm)
    return keyFac.getKeySpec(this, RSAPrivateCrtKeySpec::class.java)
  }

  private fun RSAPrivateCrtKeySpec.transform(certType: Oid, crypto: Crypto) {
    log.debug("Transformation for RSA Private Key ${cardImage.getPrivateKeyIdentifier(certType, crypto)}")
    cardImage.element(cardImage.getPrivateKeyIdentifier(certType, crypto))?.let { element ->
      element.attribute(C.attributeId).textContent = this.crtCoefficient.asHex()
      element.attribute(D.attributeId).textContent = this.privateExponent.asHex()
      element.attribute(DP.attributeId).textContent = this.primeExponentP.asHex()
      element.attribute(DQ.attributeId).textContent = this.primeExponentQ.asHex()
      element.attribute(E.attributeId).textContent = this.publicExponent.asHex()
      element.attribute(N.attributeId).textContent = this.modulus.asHex()
      element.attribute(P_RSA.attributeId).textContent = this.primeP.asHex()
      element.attribute(C.attributeId).textContent = this.primeQ.asHex()
    }
  }

  private fun ECPrivateKey.transform(certType: Oid, crypto: Crypto, certificate: X509CertificateWrapper) {
    log.debug("Transformation for ECC Private Key ${cardImage.getPrivateKeyIdentifier(certType, crypto)}")
    cardImage.element(cardImage.getPrivateKeyIdentifier(certType, crypto))?.let { element ->
      element.attribute(PRIVATE_KEY.attributeId).textContent = this.s.asHex()
      element.attribute(P_ECC.attributeId).textContent = certificate.toCertificate().publicKey.encoded.asHex()
    }
  }

  private fun X509CertificateWrapper.transform(certType: Oid, crypto: Crypto) {
    log.debug("Transformation for Public Key ${cardImage.getPublicCertIdentifier(certType, crypto)}")
    val pubAsHex = this.toCertificate().encoded.asHex()

    val length = pubAsHex.length.toString(16).uppercase().let {
      if (it.length.mod(2) == 1) {
        "0${it}"
      } else {
        it
      }
    }

    cardImage.element(cardImage.getPublicCertIdentifier(certType, crypto))?.let { element ->
      element.attribute(Attribute.BODY.attributeId).textContent = pubAsHex
      element.attribute(Attribute.POSITION_LOGICAL_END_OF_FILE.attributeId).textContent = length
    }
  }


  override fun transform(outputPath: Path) {
    val folder = outputPath.resolve("CardImages")
    Files.createDirectories(folder)
    log.info("Transform certificates to ${cardImage.cardType.name.lowercase()} for $iccsn")

    certElements.forEach {
      it.certificate.transform(it.getCertTypeOid(), it.getCrypto())
      when (it.privateKey) {
        is RSAPrivateKey -> it.privateKey.asRsaPrivateCrtKey()?.transform(it.getCertTypeOid(), it.getCrypto())
        is ECPrivateKey -> it.privateKey.transform(it.getCertTypeOid(), it.getCrypto(), it.certificate)
      }
    }

    cardImage.element(Attribute.EF_GDO.attributeId)?.let { element ->
      element.attribute(Attribute.BODY.attributeId).textContent = "5A0A${iccsn}"
    }
    cardImage.attribute(Attribute.ICCSN.attributeId).textContent = iccsn

    cardImage.save(folder.resolve(Path.of(outputFilename)))
  }
}

