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

package de.gematik.test.erezept.fhir.r4.erp.tprescription;

import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.DISPENSEINFORMATION;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.profiles.definitions.GemErpTPrescStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.profiles.version.TPrescrVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import java.util.List;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ErpTPrescriptionCarbonCopyTest extends ErpFhirParsingTest {

  private static ErpTPrescriptionCarbonCopy erpTPrescriptionCarbonCopy;

  @BeforeAll
  static void setUp() {
    erpTPrescriptionCarbonCopy = new ErpTPrescriptionCarbonCopy();
    erpTPrescriptionCarbonCopy
        .getMeta()
        .addProfile(GemErpTPrescStructDef.CARBON_COPY.getVersionedUrl(TPrescrVersion.V1_1));
    erpTPrescriptionCarbonCopy.addParameter("rxPrescription", 0);
    erpTPrescriptionCarbonCopy.addParameter("rxDispensation", 1);
    erpTPrescriptionCarbonCopy
        .getParameter("rxPrescription")
        .setPart(
            List.of(
                new Parameters.ParametersParameterComponent()
                    .setName("medication")
                    .setResource(
                        new ErpTPrescriptionMedication()
                            .setForm(Darreichungsform.HKP.asCodeableConcept())),
                new Parameters.ParametersParameterComponent()
                    .setName("prescriptionId")
                    .setValue(ErpWorkflowNamingSystem.PRESCRIPTION_ID.asIdentifier("123"))));

    erpTPrescriptionCarbonCopy
        .getParameter("rxDispensation")
        .setPart(
            List.of(
                new Parameters.ParametersParameterComponent()
                    .setName(DISPENSEINFORMATION.getCode())
                    .setPart(
                        List.of(
                            new Parameters.ParametersParameterComponent()
                                .setName("medication")
                                .setResource(
                                    new ErpTPrescriptionMedication()
                                        .setForm(Darreichungsform.PUE.asCodeableConcept()))))));
  }

  @Test
  void getDescription() {
    assertNotNull(erpTPrescriptionCarbonCopy.getDescription());
  }

  @Test
  void getMedicationFromPrescription() {
    assertNotNull(erpTPrescriptionCarbonCopy.getMedicationFromPrescription());
  }

  @Test
  void getMedicationFromDispensation() {
    assertNotNull(erpTPrescriptionCarbonCopy.getMedicationFromDispensation());
  }

  @Test
  void shouldGetCorrectPrescriptionId() {
    assertNotNull(erpTPrescriptionCarbonCopy.getPrescriptionId());
    assertEquals("123", erpTPrescriptionCarbonCopy.getPrescriptionId().getValue());
  }

  @Test
  void shouldGetDarreichungsFormFromPrescription() {
    assertEquals(
        Darreichungsform.HKP,
        erpTPrescriptionCarbonCopy.getMedicationFromPrescription().getDarreichungsform());
  }

  @Test
  void shouldGetDarreichungsFormFromDipensation() {
    assertEquals(
        Darreichungsform.PUE,
        erpTPrescriptionCarbonCopy.getMedicationFromDispensation().getDarreichungsform());
  }

  @Test
  void shouldGetDarreichungsFormFromPrescription2() {
    assertNotEquals(
        Darreichungsform.KPG,
        erpTPrescriptionCarbonCopy.getMedicationFromPrescription().getDarreichungsform());
  }

  @Test
  void shouldGetDarreichungsFormFromDipensation2() {
    assertNotEquals(
        Darreichungsform.KPG,
        erpTPrescriptionCarbonCopy.getMedicationFromDispensation().getDarreichungsform());
  }
}
