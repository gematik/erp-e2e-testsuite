/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.primsys.mapping;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

class PznDispensedMedicationDataMapperTest extends ParsingTest {

  @RepeatedTest(value = 10)
  void shouldGenerateRandomly() {
    val mapper = PznDispensedMedicationDataMapper.random();
    val medicationDispense = mapper.convert();
    val vr = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(vr.isSuccessful());
  }
}
