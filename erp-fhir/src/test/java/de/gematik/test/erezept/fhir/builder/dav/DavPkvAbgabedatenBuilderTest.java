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

package de.gematik.test.erezept.fhir.builder.dav;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DavPkvAbgabedatenBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build DAV Abgabedatenbundle with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#abdaErpPkvVersions")
  void buildDavAbgabedatenBundleWithFixedValues(AbdaErpPkvVersion version) {
    val prescriptionId = PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_200);
    val kbvBundle = KbvErpBundleFaker.builder().withPrescriptionId(prescriptionId).fake();

    val pharmacy =
        PharmacyOrganizationBuilder.builder()
            .version(version)
            .name("Adler-Apotheke")
            .iknr("757299999")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val pc1 =
        PriceComponentBuilder.builder(KostenVersicherterKategorie.ZUZAHLUNG)
            .version(version)
            .currency(Currency.EUR) // EUR by default
            .type("informational")
            .insurantCost(5.8f)
            .totalCost(289.99f)
            .build();

    val invoice =
        DavInvoiceBuilder.builder()
            .version(version)
            .currency(Currency.EUR) // EUR by default
            .status("issued")
            .vatRate(19.0f)
            .addPriceComponent(pc1, "12345678", "Test-Medikament")
            .build();

    val dispensedMedication =
        DavPkvDispensedMedicationBuilder.builder()
            .version(version)
            .status("completed")
            .prescription(prescriptionId)
            .pharmacy(pharmacy)
            .invoice(invoice)
            .build();

    val davBundle =
        DavPkvAbgabedatenBuilder.builder(kbvBundle)
            .version(version)
            .pharmacy(pharmacy)
            .medication(dispensedMedication)
            .invoice(invoice)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, davBundle);
    assertTrue(result.isSuccessful());
  }

  @RepeatedTest(5)
  void shouldBuildValidWithFaker() {
    val davBundle = DavPkvAbgabedatenFaker.builder().fake();
    val result = ValidatorUtil.encodeAndValidate(parser, davBundle);
    assertTrue(result.isSuccessful());
  }
}
