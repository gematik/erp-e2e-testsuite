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

package de.gematik.test.erezept.apovzd.fhir

import de.gematik.test.erezept.apovzd.TestDataPharmacy
import de.gematik.test.erezept.crypto.certificate.X509CertificateWrapper
import org.hl7.fhir.r4.model.Binary
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Reference


class EncCertBinary(private val apoReference: String, private val cert: X509CertificateWrapper) {
  fun build() = newResource(::Binary).also {
    it.securityContext = Reference(apoReference)
    it.contentType = "application/pkix-cert"
    it.data = cert.toCertificate().encoded
  }
}

class EncCertBundle(private val testdata: List<TestDataPharmacy>) {
  fun build() = newBundle(::Bundle, total = testdata.flatMap { it.certs }.size).also { bundle ->
    testdata.forEach { testdata ->
      val ref = testdata.apoReference
      testdata.certs.forEach { cert ->
        bundle.entry.add(EncCertBinary(ref, cert).build().toComponent())
      }
    }
  }
}

