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

package de.gematik.test.erezept.primsys.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvHealthAppRequestFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPatientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPractitionerFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.primsys.data.HealthAppRequestDto;
import lombok.val;
import org.junit.jupiter.api.Test;

class HealthAppRequestDataMapperTest extends ErpFhirParsingTest {

  @Test
  void shouldMapFromHealthAppRequest() {
    val har = KbvHealthAppRequestFaker.forRandomPatient().fake();
    val mapper = HealthAppRequestDataMapper.from(har).build();
    val dto = mapper.getDto();

    assertEquals(har.getPzn().getValue(), dto.getPzn());
    assertEquals(har.getName(), dto.getName());
    assertEquals(har.relatesToSocialCompensationLaw(), dto.isSer());
  }

  @Test
  void shouldMapFromDto() {
    val dto =
        HealthAppRequestDto.builder()
            .relatesToSocialCompensationLaw(false)
            .pzn("19205615")
            .name("Vantis KHK und Herzinfarkt 001")
            .build();

    val patient = KbvPatientFaker.builder().fake();
    val practitioner = KbvPractitionerFaker.builder().fake();
    val coverage = KbvCoverageFaker.builder().fake();
    val mapper =
        HealthAppRequestDataMapper.from(dto)
            .requestedFor(patient)
            .requestedBy(practitioner)
            .coveredBy(coverage)
            .build();
    val har = mapper.convert();

    assertEquals(har.getPzn().getValue(), dto.getPzn());
    assertEquals(har.getName(), dto.getName());
    assertEquals(har.relatesToSocialCompensationLaw(), dto.isSer());
  }
}
