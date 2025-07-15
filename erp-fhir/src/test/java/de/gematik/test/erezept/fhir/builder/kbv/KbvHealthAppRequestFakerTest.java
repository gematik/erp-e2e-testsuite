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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.github.javafaker.service.RandomService;
import de.gematik.bbriccs.fhir.builder.FakerBrick;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.AccidentCauseType;
import java.util.Locale;
import java.util.Random;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junitpioneer.jupiter.SetSystemProperty;

@SetSystemProperty(
    key = ERP_FHIR_PROFILES_TOGGLE,
    value = "1.4.0") // before 1.4.0 EVDGA was not available
class KbvHealthAppRequestFakerTest extends ErpFhirParsingTest {

  @Test
  void shouldFakeHealthAppRequest() {
    val har = KbvHealthAppRequestFaker.forRandomPatient().fake();

    val result = ValidatorUtil.encodeAndValidate(parser, har);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(name = "[{index}] -> HealthAppRequest with Accident Extension for {0}")
  @EnumSource(AccidentCauseType.class)
  void shouldFakeHealthAppRequestWithAccident(AccidentCauseType causeType) {
    val rnd = mock(Random.class);
    val rndService = new RandomService(rnd);
    val fakerBrick = spy(new FakerBrick(Locale.GERMAN, rnd));
    when(fakerBrick.random()).thenReturn(rndService);

    // for accident always true
    when(rnd.nextBoolean()).thenReturn(true);
    when(fakerBrick.randomEnum(AccidentCauseType.class)).thenReturn(causeType);

    try (val fakerBrickMocker = mockStatic(FakerBrick.class)) {
      fakerBrickMocker.when(FakerBrick::getGerman).thenReturn(fakerBrick);

      val har = KbvHealthAppRequestFaker.forRandomPatient().fake();
      val result = ValidatorUtil.encodeAndValidate(parser, har);
      assertTrue(result.isSuccessful());
    }
  }
}
