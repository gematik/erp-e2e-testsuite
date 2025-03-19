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

package de.gematik.test.erezept.fhir.values;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.val;

public interface InsuranceCoverageInfo {
  String getName();

  String getIknr();

  InsuranceTypeDe getInsuranceType();

  static List<Class<? extends InsuranceCoverageInfo>> getImplementors() {
    return List.of(
        GkvInsuranceCoverageInfo.class,
        PkvInsuranceCoverageInfo.class,
        BGInsuranceCoverageInfo.class);
  }

  static <T extends InsuranceCoverageInfo> Optional<T> getByIknr(Class<T> clazz, String iknr) {
    return Arrays.stream(clazz.getEnumConstants())
        .filter(element -> element.getIknr().equals(iknr))
        .findFirst();
  }

  static Optional<InsuranceCoverageInfo> getByIknr(String iknr) {
    val implementors = getImplementors();

    Optional<InsuranceCoverageInfo> result = Optional.empty();
    for (val impl : implementors) {
      val intermediateResult = getByIknr(impl, iknr);
      if (intermediateResult.isPresent()) {
        result = Optional.of(intermediateResult.get());
        break;
      }
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  static <T extends InsuranceCoverageInfo> Optional<Class<T>> coverageOptionsFor(
      InsuranceTypeDe insuranceKind) {
    return switch (insuranceKind) {
      case GKV -> Optional.of((Class<T>) GkvInsuranceCoverageInfo.class);
      case PKV -> Optional.of((Class<T>) PkvInsuranceCoverageInfo.class);
      case BG -> Optional.of((Class<T>) BGInsuranceCoverageInfo.class);
      default -> Optional.empty();
    };
  }

  static InsuranceCoverageInfo randomFor(InsuranceTypeDe insuranceKind) {
    return coverageOptionsFor(insuranceKind)
        .map(options -> GemFaker.randomElement(options.getEnumConstants()))
        .stream()
        .findFirst()
        .orElse(DynamicInsuranceCoverageInfo.random());
  }

  static String shortenName(String name) {
    if (name.length() > 45) return name.substring(0, 45);
    return name;
  }
}
