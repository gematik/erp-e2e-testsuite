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


package de.gematik.test.erezept

import de.gematik.test.erezept.crypto.certificate.Oid
import de.gematik.test.erezept.crypto.certificate.X509CertificateWrapper
import de.gematik.test.erezept.transformation.CardType
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate


fun Path.getCertificateElements(): List<CertificateElement<PrivateKey>> = Files.walk(this)
        .filter { Files.isRegularFile(it) }
        // currently only for p12 files
        .filter { it.toString().endsWith(".p12") }
        .map { it.loadEntryFromKeystore() }.toList()

private fun Path.loadEntryFromKeystore(): CertificateElement<PrivateKey> {
  val entry = FileInputStream(this.toFile()).loadEntryFromKeystore("00")
  val cert = X509CertificateWrapper(entry.certificate as X509Certificate)
  return CertificateElement(
          entry.privateKey,
          cert,
          this)
}

private fun FileInputStream.loadEntryFromKeystore(
        password: String,
): KeyStore.PrivateKeyEntry {
  val ks = KeyStore.getInstance("PKCS12")
  ks.load(this, password.toCharArray())
  val alias = ks.aliases()
          .nextElement()
  return ks.getEntry(alias, KeyStore.PasswordProtection(password.toCharArray())) as KeyStore.PrivateKeyEntry
}

enum class Crypto(val identifier: String) {
  RSA("R2048"),
  ECC("E256"),
}

enum class CertType(vararg val oid: Oid) {
  AUT(Oid.OID_EGK_AUT, Oid.OID_EGK_AUT_ALT, Oid.OID_HBA_AUT, Oid.OID_SMC_B_AUT),
  ENC(Oid.OID_HBA_ENC, Oid.OID_SMC_B_ENC),
  OSIG(Oid.OID_SMC_B_OSIG),
  QES(Oid.OID_HBA_QES)
}

class CertificateElement<P : PrivateKey>(
        val privateKey: P,
        val certificate: X509CertificateWrapper,
        val pathP12: Path,
) {
  fun getCertTypeOid() = certificate.certificateTypeOid.orElseThrow()

  fun getCertType(): CertType {
    val oid = getCertTypeOid()
    return CertType.values().first { it.oid.contains(oid) }
  }

  fun getCrypto() = if (certificate.isRsaEncryption) Crypto.RSA else Crypto.ECC

  fun getCardType(): CardType = this.certificate.certificateTypeOid.orElseThrow().let { oid ->
    CardType.values().first { oid.name.contains(it.name.lowercase(), true) }
  }

  fun getIccsn() = pathP12.getIccsn() ?: certificate.getIccsn()
  ?: throw IccsnException("ICCSN could not be determined")

  private fun Path.getIccsn() = this.fileName.toString().split(Regex("[-_]")).let {
    if (it.isNotEmpty() && it.size >= 2 && it.first().toBigIntegerOrNull() != null)
      it.first()
    else
      null
  }

  private fun X509CertificateWrapper.getIccsn(): String? = this.toCertificateHolder().subject.let { subject ->
    val iccsn = subject.rdNs.flatMap { it.typesAndValues.asIterable() }
            .filter { it.type == ASN1ObjectIdentifier("2.5.4.5") }
            .map { it.value }
            .firstOrNull() ?: return null
    iccsn.toString().substringAfterLast(".")
  }

  fun getKvid(): String {
    require(getCardType() == CardType.EGK)

    val value = certificate.toCertificateHolder().subject.let { subject ->
      subject.rdNs.flatMap { it.typesAndValues.asIterable() }
        .filter { it.type == ASN1ObjectIdentifier("2.5.4.11") }
        .map { it.value }
        .first { it.toString().length == 10 }
    }
    return value.toString()
  }

  fun getName(): String {
    return certificate.toCertificateHolder().subject.rdNs.flatMap { it.typesAndValues.asIterable() }
      .filter { it.type == ASN1ObjectIdentifier("2.5.4.4") || it.type == ASN1ObjectIdentifier("2.5.4.42") }
      .map { it.value }
      .joinToString()
  }

}
