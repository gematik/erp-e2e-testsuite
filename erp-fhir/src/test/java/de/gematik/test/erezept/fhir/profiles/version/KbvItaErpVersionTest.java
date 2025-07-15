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

package de.gematik.test.erezept.fhir.profiles.version;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;

class KbvItaErpVersionTest extends ErpFhirBuildingTest {

  @Test
  @ClearSystemProperty(key = "kbv.ita.erp")
  void getDefaultVersionViaSystemProperty() {
    Arrays.stream(KbvItaErpVersion.values())
        .forEach(
            version -> {
              System.setProperty("kbv.ita.erp", version.getVersion());
              val defaultVersion = KbvItaErpVersion.getDefaultVersion();
              assertEquals(version, defaultVersion);
            });
  }
}
