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

package de.gematik.test.erezept.fhir.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
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

    return Stream.of(Arguments.of("1.4.0"));
  }

  public static Stream<Arguments> erpWorkflowVersions() {
    return Arrays.stream(ErpWorkflowVersion.values())
        .filter(version -> version.compareTo(ErpWorkflowVersion.V1_4) >= 0)
        .map(Arguments::of);
  }

  public static Stream<Arguments> kbvItaErpVersions() {
    return Arrays.stream(KbvItaErpVersion.values()).map(Arguments::of);
  }

  public static Stream<Arguments> kbvItaForVersions() {
    return Arrays.stream(KbvItaForVersion.values()).map(Arguments::of);
  }

  public static Stream<Arguments> kbvBundleVersions() {
    val itaErp = kbvItaErpVersions().map(arguments -> arguments.get()[0]).toList();
    val itaFor = kbvItaForVersions().map(arguments -> arguments.get()[0]).toList();

    assertEquals(itaFor.size(), itaErp.size());
    return IntStream.range(0, itaErp.size())
        .mapToObj(i -> Arguments.of(itaFor.get(i), itaErp.get(i)));
  }

  public static Stream<Arguments> abdaErpPkvVersions() {
    return Arrays.stream(AbdaErpPkvVersion.values()).map(Arguments::of);
  }
}
