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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.parser.ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.Date;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.DeviceRequest.DeviceRequestStatus;
import org.hl7.fhir.r4.model.DeviceRequest.RequestIntent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.SetSystemProperty;

@SetSystemProperty(
    key = ERP_FHIR_PROFILES_TOGGLE,
    value = "1.4.0") // before 1.4.0 EVDGA was not available
class KbvHealthAppRequestBuilderTest extends ErpFhirParsingTest {

  @Test
  void shouldBuildHealthAppRequestWithFixedValues() {
    val har =
        KbvHealthAppRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .insurance(KbvCoverageFaker.builder().fake())
            .requester(KbvPractitionerFaker.builder().fake())
            .healthApp("17946626", "HelloBetter Schmerzen 001")
            .status(DeviceRequestStatus.ACTIVE)
            .intent(RequestIntent.ORDER)
            .authoredOn(new Date())
            .relatesToSocialCompensationLaw(false)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, har);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest
  @MethodSource
  void shouldBuildHealthAppRequestWithFixedValuesWithAccident(AccidentExtension accidentExtension) {
    val har =
        KbvHealthAppRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .insurance(KbvCoverageFaker.builder().fake())
            .requester(KbvPractitionerFaker.builder().fake())
            .healthApp("17946626", "HelloBetter Schmerzen 001")
            .accident(accidentExtension)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, har);
    assertTrue(result.isSuccessful());
  }

  static Stream<Arguments> shouldBuildHealthAppRequestWithFixedValuesWithAccident() {
    return Stream.of(
            AccidentExtension.accident(),
            AccidentExtension.accidentAtWork().atWorkplace(),
            AccidentExtension.occupationalDisease())
        .map(Arguments::of);
  }
}
