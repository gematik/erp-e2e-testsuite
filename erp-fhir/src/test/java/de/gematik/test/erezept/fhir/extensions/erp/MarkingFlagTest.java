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

package de.gematik.test.erezept.fhir.extensions.erp;

import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MarkingFlagTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build MarkingFlags Parameters using new profiles {0}")
  @ValueSource(booleans = {true, false})
  void shouldGenerateAsParameters(boolean useNewProfile) {
    val flags = MarkingFlag.with(true, false, true);
    Parameters parameters;

    if (useNewProfile) {
      parameters = flags.asParameters(PatientenrechnungVersion.V1_0_0);
    } else {
      parameters = flags.asParameters();
    }

    ValidatorUtil.encodeAndValidate(parser, parameters, EncodingType.JSON);
  }
}
