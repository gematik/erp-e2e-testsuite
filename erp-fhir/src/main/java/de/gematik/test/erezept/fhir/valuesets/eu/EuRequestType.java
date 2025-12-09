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

package de.gematik.test.erezept.fhir.valuesets.eu;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.test.erezept.fhir.profiles.systems.GemErpEuCodeSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("java:S6548")
public enum EuRequestType implements FromValueSet {
  DEMOGRAPHICS(
      "demographics", "requesting demographics by one prescription to validate Patients identity"),
  PRESCRIPTION_RETRIEVAL(
      "e-prescriptions-retrieval",
      "Query and accept a specific list of EU redeemable prescriptions in the corresponding EU"
          + " country"),
  PRESCRIPTION_LIST(
      "e-prescriptions-list", "Query all redeemable prescriptions in the corresponding EU country"),
  ;

  public static final GemErpEuCodeSystem CODE_SYSTEM = GemErpEuCodeSystem.REQUEST_TYPE;

  private final String code;
  private final String display;

  @Override
  public GemErpEuCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static EuRequestType fromCode(String code) {
    return FromValueSet.fromCode(EuRequestType.class, code);
  }
}
