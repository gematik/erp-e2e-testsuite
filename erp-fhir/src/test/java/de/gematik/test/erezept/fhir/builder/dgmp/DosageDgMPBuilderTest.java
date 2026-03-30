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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.builder.dgmp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.erp.*;
import de.gematik.test.erezept.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.BmpDosiereinheit;
import java.math.BigDecimal;
import lombok.val;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Test;

class DosageDgMPBuilderTest extends ErpFhirParsingTest {

  @Test
  void buildSimpleDosage() {

    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder("Tablette", BmpDosiereinheit.MG)
            .text("1 Tablette morgens")
            .value(new BigDecimal(1))
            .build();
    assertNotNull(dosage);
    assertEquals("1 Tablette morgens", dosage.getText());
    assertEquals(1, dosage.getDoseAndRate().size());
    assertEquals(
        "v",
        ((org.hl7.fhir.r4.model.SimpleQuantity) dosage.getDoseAndRate().get(0).getDose())
            .getCode());
    assertEquals(
        "Tablette",
        ((org.hl7.fhir.r4.model.SimpleQuantity) dosage.getDoseAndRate().get(0).getDose())
            .getUnit());
    val medDisp =
        ErxMedicationDispenseFaker.builder()
            .withPrescriptionId(PrescriptionId.random().getValue())
            .withDgmp(dosage)
            .fake();

    assertTrue(ValidatorUtil.encodeAndValidate(parser, medDisp).isSuccessful());
  }

  @Test
  void buildSimpleDosageWithTiming() {

    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder("Tablette", BmpDosiereinheit.MG)
            .value(new BigDecimal(1))
            .timing(3, 2, Timing.UnitsOfTime.D)
            .build();
    val medDisp =
        ErxMedicationDispenseFaker.builder()
            .withPrescriptionId(PrescriptionId.random().getValue())
            .withDgmp(dosage)
            .fake();

    assertTrue(ValidatorUtil.encodeAndValidate(parser, medDisp).isSuccessful());
  }
}
