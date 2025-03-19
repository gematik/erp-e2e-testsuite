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
 */

package de.gematik.test.erezept.fhir.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.provider.Arguments;

// TODO: move these methods to ErpFhirBuilding / ParsingTest
public class VersionArgumentProvider {
  /**
   * caused by a breakingChange from ErpWorkflowVersion.V1_3_0 to ErpWorkflowVersion.V1_4_0 in this
   * case you´ll get only V1_2_0 & V1_3_0
   *
   * @return ErpWorkflowVersion.V1_2_0, ErpWorkflowVersion.V1_3_0
   */
  public static Stream<Arguments> erpFhirProfileVersions() {
    // TODO: get rid of the support for building 1.1.1 profiles
    //    return Stream.of(Arguments.of("1.1.1"), Arguments.of("1.2.0"));
    return Stream.of(
        //        Arguments.of("1.2.0"),
        Arguments.of("1.3.0"), Arguments.of("1.4.0"));
  }

  public static Stream<Arguments> oldErpFhirProfileVersions() {
    // TODO: get rid of the support for building 1.1.1 profiles   commanded out, until builder for
    // gemMedication ist seperated ...
    //    return Stream.of(Arguments.of("1.1.1"), Arguments.of("1.2.0"));
    return Stream.of(Arguments.of("1.2.0"), Arguments.of("1.3.0"));
  }

  public static Stream<Arguments> erpWorkflowVersions() {
    return Arrays.stream(ErpWorkflowVersion.values())
        .filter(v -> !v.equals(ErpWorkflowVersion.V1_1_1))
        .map(Arguments::of);
  }

  public static Stream<Arguments> oldErpWorkflowVersions() {
    return Arrays.stream(ErpWorkflowVersion.values())
        .filter(v -> !v.equals(ErpWorkflowVersion.V1_1_1))
        .filter(v -> !v.equals(ErpWorkflowVersion.V1_4_0))
        .map(Arguments::of);
  }

  public static Stream<Arguments> kbvItaErpVersions() {
    return Arrays.stream(KbvItaErpVersion.values())
        //        .filter(v -> !v.equals(KbvItaErpVersion.V1_0_2))
        .map(Arguments::of);
  }

  public static Stream<Arguments> kbvItaForVersions() {
    return Arrays.stream(KbvItaForVersion.values())
        //        .filter(v -> !v.equals(KbvItaForVersion.V1_0_3))
        .map(Arguments::of);
  }

  public static Stream<Arguments> kbvBundleVersions() {
    val itaErp = kbvItaErpVersions().map(arguments -> arguments.get()[0]).toList();
    val itaFor = kbvItaForVersions().map(arguments -> arguments.get()[0]).toList();

    assertEquals(itaFor.size(), itaErp.size());
    return IntStream.range(0, itaErp.size())
        .mapToObj(i -> Arguments.of(itaFor.get(i), itaErp.get(i)));
  }

  public static Stream<Arguments> abdaErpPkvVersions() {
    return Arrays.stream(AbdaErpPkvVersion.values())
        .filter(v -> !v.equals(AbdaErpPkvVersion.V1_1_0)) // not supported!!
        .map(Arguments::of);
  }
}
