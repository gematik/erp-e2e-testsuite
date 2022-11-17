/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.kbv;

import static java.text.MessageFormat.format;
import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

@Slf4j
public class CoverageBuilderTest extends ParsingTest {

  @Test
  public void buildCoverageWithFaker01() {
    for (var i = 0; i < 5; i++) {
      val coverage = CoverageBuilder.faker().build();
      log.info(format("Validating Faker Coverage with ID {0}", coverage.getId()));
      val result = ValidatorUtil.encodeAndValidate(parser, coverage);
      assertTrue(result.isSuccessful());
    }
  }

  @Test
  public void buildCoverageWithFaker02() {
    val insuranceKinds = List.of(VersicherungsArtDeBasis.GKV, VersicherungsArtDeBasis.PKV);

    insuranceKinds.forEach(
        ik -> {
          for (var i = 0; i < 5; i++) {
            val coverage = CoverageBuilder.faker(ik).build();
            log.info(format("Validating Faker Coverage with ID {0}", coverage.getId()));
            val result = ValidatorUtil.encodeAndValidate(parser, coverage);
            assertTrue(result.isSuccessful());
          }
        });
  }
}
