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

package de.gematik.test.erezept.apovzd

import de.gematik.test.erezept.apovzd.fhir.ApoLocationBundle
import de.gematik.test.erezept.apovzd.fhir.EncCertBundle
import de.gematik.test.erezept.apovzd.fhir.toJson
import de.gematik.test.erezept.crypto.certificate.Oid
import de.gematik.test.erezept.crypto.certificate.X509CertificateWrapper
import de.gematik.test.smartcard.Crypto
import de.gematik.test.smartcard.SmartcardFactory
import picocli.CommandLine
import java.io.File
import java.util.*
import java.util.concurrent.Callable
import kotlin.system.exitProcess

data class TestDataPharmacy(
  val apoId: UUID = UUID.randomUUID(),
  val apoName: String,
  val certs: List<X509CertificateWrapper>
) {
  val apoReference: String = "Location/$apoId"
}

@CommandLine.Command(
  name = "generate", mixinStandardHelpOptions = true,
  description = ["Generate ApoVZD artefacts for Test-Pharmacies"]
)
class Generator : Callable<Int> {

  @CommandLine.Option(names = ["-a", "--algorithm"], description = ["RSA, ECC"])
  var algorithm = "RSA"

  override fun call(): Int {
    val cryptoAlgorithm = Crypto.fromString(algorithm);
    val testdata = SmartcardFactory.getArchive().smcbCards
      .asSequence()
      .map { it.getKey(Oid.OID_SMC_B_ENC, cryptoAlgorithm) }
      .filter { it.isPresent }
      .map { it.get().certWrapper }
      .groupBy { it.professionId.get() }
      .map { TestDataPharmacy(apoName = it.key, certs = it.value) }
      .toList()

    val encCertBundles = EncCertBundle(testdata).build().toJson()
    File("tools/ApoVZDTestData/target/BinaryBundle.json").writeText(encCertBundles)

    val locationBundles = ApoLocationBundle(testdata).build().toJson()
    File("tools/ApoVZDTestData/target/LocationBundle.json").writeText(locationBundles)
    return 0;
  }
}

fun main(args: Array<String>): Unit = exitProcess(CommandLine(Generator()).execute(*args))
