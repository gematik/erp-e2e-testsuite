/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fhir.valuesets;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReadyForCollecting {
  IMMEDIATELY("immediately", "Immediately"),
  SAME_DAY("sameDay", "Same day"),
  NEXT_DAY("nextDay", "Next day"),
  NEXT_DAY_AM("nextDayAM", "Next day AM"),
  NEXT_DAY_PM("nextDayPM", "Next day PM"),
  UNKNOWN("unknown", "Unknown"),
  NOT_AVAILABLE("notAvailable", "Not available");

  private final String code;
  private final String display;

  public static ReadyForCollecting fromCode(String code) {
    return Arrays.stream(ReadyForCollecting.values())
        .filter(rfc -> rfc.code.equals(code))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unknown ReadyForCollecting code: " + code));
  }
}
