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

package de.gematik.test.erezept.apovzd.fhir

import de.gematik.test.erezept.apovzd.TestDataPharmacy
import de.gematik.test.erezept.crypto.certificate.X509CertificateWrapper
import de.gematik.test.erezept.fhir.builder.AddressBuilder
import de.gematik.test.erezept.fhir.builder.GemFaker
import de.gematik.test.erezept.fhir.values.TelematikID
import de.gematik.test.erezept.pspwsclient.dataobjects.DeliveryOption
import de.gematik.test.smartcard.SmartcardOwnerData
import de.gematik.test.smartcard.cfg.LdapReader
import org.hl7.fhir.r4.model.*
import org.hl7.fhir.r4.model.Location.DaysOfWeek
import java.util.*

class ApoLocation(private val apoId: UUID, private val cert: X509CertificateWrapper) {

  private val url = "https://erp-pharmacy-serviceprovider.dev.gematik.solutions"
  fun build() = newResource(::Location, apoId).also { location ->
    val data = LdapReader.getOwnerData(cert.toCertificate().getSubjectX500Principal())
    location.name = data.commonName
    location.address = data.toAddress(data)

    val telematikID = cert.professionId.orElseThrow()
    location.telecom.let {
      it.add(DeliveryOption.SHIPMENT.toContactPoint(url, telematikID))
      it.add(DeliveryOption.DELIVERY.toContactPoint(url, telematikID))
      it.add(DeliveryOption.ON_PREMISE.toContactPoint(url, telematikID))
    }
    location.position = toPosition()
    location.identifier.add(TelematikID.from(cert.professionId.orElseThrow()).asIdentifier())
    location.status = Location.LocationStatus.ACTIVE
    val typeSystemRoleCode = "http://terminology.hl7.org/CodeSystem/v3-RoleCode"
    location.type.let {
      it.addAll(
        listOf(
          toCodeableConcept(
            "http://terminology.hl7.org/CodeSystem/service-type",
            "DELEGATOR",
            "eRX Token Receiver"
          ),
          toCodeableConcept(
            typeSystemRoleCode,
            "PHARM",
            "pharmacy"
          ),
          toCodeableConcept(
            typeSystemRoleCode,
            "OUTPHARM",
            "outpatient pharmacy"
          ),
          toCodeableConcept(
            typeSystemRoleCode,
            "MOBL",
            "Mobile Services"
          )
        )
      )
    }
    location.hoursOfOperation.add(Location.LocationHoursOfOperationComponent().also {
      it.addDaysOfWeek(DaysOfWeek.MON)
      it.addDaysOfWeek(DaysOfWeek.TUE)
      it.addDaysOfWeek(DaysOfWeek.WED)
      it.addDaysOfWeek(DaysOfWeek.THU)
      it.addDaysOfWeek(DaysOfWeek.FRI)
      it.openingTime = "08:00:00"
      it.closingTime = "18:00:00"
    })
  }

  private fun toPosition(): Location.LocationPositionComponent = Location.LocationPositionComponent(
    DecimalType(48.3536968), DecimalType(10.9447565)
  )

  private fun SmartcardOwnerData.toAddress(data: SmartcardOwnerData): Address? {
    val city = data.locality ?: GemFaker.fakerCity()
    val postalCode = data.postalCode ?: GemFaker.fakerZipCode()
    val street = data.street ?: GemFaker.fullStreetName(false)
      return AddressBuilder.address(
          city,
          postalCode,
          street,
          Address.AddressType.PHYSICAL
      )
  }

  private fun toCodeableConcept(system: String, code: String, display: String) =
    CodeableConcept().also {
      it.setCoding(listOf(Coding(system, code, display)))
    }

  private fun DeliveryOption.toContactPoint(url: String, id: String) = ContactPoint().also {
    it.system = ContactPoint.ContactPointSystem.OTHER
    it.value = "$url/${this.path}/$id"
    it.use = ContactPoint.ContactPointUse.MOBILE
    it.rank = when (this) {
      DeliveryOption.DELIVERY -> 200
      DeliveryOption.SHIPMENT -> 300
      DeliveryOption.ON_PREMISE -> 100
    }
  }
}


class ApoLocationBundle(private val testdata: List<TestDataPharmacy>) {
  fun build() = newBundle(::Bundle, total = testdata.size).also { bundle ->
    testdata.forEach { testdata ->
      testdata.certs.forEach { cert ->
        bundle.entry.add(ApoLocation(testdata.apoId, cert).build().toComponent())
      }
    }
  }
}
