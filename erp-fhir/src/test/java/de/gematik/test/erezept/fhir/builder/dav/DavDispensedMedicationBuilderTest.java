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

package de.gematik.test.erezept.fhir.builder.dav;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class DavDispensedMedicationBuilderTest extends ParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build DAV DispensedMedication with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#abdaErpPkvVersions")
  void buildMedicationDispenseWithFixedValues(AbdaErpPkvVersion version) {
    val pharmacyId = "ce10f18e-7cce-4f67-aca5-e2050f965b60";
    val invoiceId = "ce10f18e-7cce-4f67-aca5-e2050f965b61";

    val md =
        DavDispensedMedicationBuilder.builder()
            .version(version)
            .status("completed")
            .prescription(PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_200))
            .pharmacy(pharmacyId)
            .invoice(invoiceId)
            .whenHandedOver(new Date())
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, md);
    assertTrue(result.isSuccessful());
  }
}
