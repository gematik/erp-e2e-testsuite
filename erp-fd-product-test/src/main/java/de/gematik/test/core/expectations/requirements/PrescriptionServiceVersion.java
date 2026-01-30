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

package de.gematik.test.core.expectations.requirements;

import java.util.Arrays;
import java.util.Objects;

public enum PrescriptionServiceVersion {
  V_1_19_0("1.19.0"),
  V_1_20_0("1.20.0"),
  V_1_21_0("1.21.0"),
  UNKNOWN("unknown");

  private final String version;
  private final int[] semanticVersion;

  PrescriptionServiceVersion(String version) {
    this.version = version;
    this.semanticVersion = parseSemanticVersion(version);
  }

  public static PrescriptionServiceVersion from(String version) {
    if (version == null) {
      return UNKNOWN;
    }
    return Arrays.stream(values())
        .filter(v -> Objects.equals(v.version, version))
        .findFirst()
        .orElseGet(
            () ->
                Arrays.stream(values())
                    .filter(v -> v.matchesSemanticVersion(version))
                    .findFirst()
                    .orElse(UNKNOWN));
  }

  public boolean isBetween(
      PrescriptionServiceVersion lowerInclusive, PrescriptionServiceVersion upperInclusive) {

    return this.compareTo(lowerInclusive) >= 0 && this.compareTo(upperInclusive) <= 0;
  }

  public boolean isAtLeast(PrescriptionServiceVersion version) {
    return this.semanticCompare(version) >= 0;
  }

  public boolean isLessThan(PrescriptionServiceVersion version) {
    return this.semanticCompare(version) < 0;
  }

  private boolean matchesSemanticVersion(String otherVersion) {
    int[] other = parseSemanticVersion(otherVersion);
    if (other.length == 0) {
      return false;
    }
    int length = Math.min(semanticVersion.length, other.length);
    for (int i = 0; i < length; i++) {
      if (semanticVersion[i] != other[i]) {
        return false;
      }
    }
    return true;
  }

  private int semanticCompare(PrescriptionServiceVersion other) {
    int[] a = this.semanticVersion;
    int[] b = other.semanticVersion;
    int length = Math.max(a.length, b.length);
    for (int i = 0; i < length; i++) {
      int ai = i < a.length ? a[i] : 0;
      int bi = i < b.length ? b[i] : 0;
      if (ai != bi) {
        return Integer.compare(ai, bi);
      }
    }
    return 0;
  }

  private static int[] parseSemanticVersion(String version) {
    if (version == null || version.isBlank()) {
      return new int[0];
    }
    String[] parts = version.split("\\.");
    int[] result = new int[parts.length];
    for (int i = 0; i < parts.length; i++) {
      try {
        result[i] = Integer.parseInt(parts[i]);
      } catch (NumberFormatException e) {
        return new int[0];
      }
    }
    return result;
  }
}
