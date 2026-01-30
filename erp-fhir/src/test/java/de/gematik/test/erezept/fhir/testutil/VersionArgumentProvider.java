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

import de.gematik.test.erezept.fhir.profiles.version.*;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.provider.Arguments;

// TODO: move these methods to ErpFhirBuilding / ParsingTest
public class VersionArgumentProvider {
  /** provide the version of the whole ERP-FHIR profile view */
  public static Stream<Arguments> erpFhirProfileVersions() {
    return Stream.of(Arguments.of("1.5.0"));
  }

  public static Stream<Arguments> erpWorkflowVersions() {
    val res =
        Arrays.stream(ErpWorkflowVersion.values())
            .filter(version -> version.compareTo(ErpWorkflowVersion.V1_4) >= 0)
            // toDo reactivate after fitting builder for version 1.6
            .filter(version -> !version.isBiggerThan(ErpWorkflowVersion.V1_5))
            .map(Arguments::of);
    return res;
  }

  public static Stream<Arguments> erpPatienterechnungVersions() {
    return Arrays.stream(PatientenrechnungVersion.values()).map(Arguments::of);
  }

  public static Stream<Arguments> kbvItaErpVersions() {
    return Arrays.stream(KbvItaErpVersion.values())
        .filter(vers -> !vers.isSmallerThan(KbvItaErpVersion.V1_3_0))
        // todo reduce after fitting builder for new constrains
        .filter(vers -> vers.isSmallerThan(KbvItaErpVersion.V1_4_0))
        .map(Arguments::of);
  }

  public static Stream<Arguments> kbvItaForVersions() {
    return Arrays.stream(KbvItaForVersion.values())
        .filter(vers -> !vers.isSmallerThan(KbvItaForVersion.V1_2_0))
        // todo reduce after fitting builder for new constrains
        .filter(vers -> vers.isSmallerThan(KbvItaForVersion.V1_3_0))
        .map(Arguments::of);
  }

  public static Stream<Arguments> kbvBundleVersions() {
    val itaErp = kbvItaErpVersions().map(arguments -> arguments.get()[0]).toList();
    val itaFor = kbvItaForVersions().map(arguments -> arguments.get()[0]).toList();

    assertEquals(
        itaFor.size(),
        itaErp.size(),
        "different enum length detected @ KbvItaForVersion & KbvItaErpVersion");
    // if different version sizes in here you´ll get an index out of bounds
    return IntStream.range(0, itaErp.size())
        .filter(i -> !itaErp.get(i).equals(KbvItaErpVersion.V1_1_0))
        .mapToObj(i -> Arguments.of(itaFor.get(i), itaErp.get(i)));
  }

  public static Stream<Arguments> abdaErpPkvVersions() {
    return Arrays.stream(AbdaErpPkvVersion.values())
        .filter(it -> it.isBiggerThan(AbdaErpPkvVersion.V1_2_0))
        .map(Arguments::of);
  }
}
