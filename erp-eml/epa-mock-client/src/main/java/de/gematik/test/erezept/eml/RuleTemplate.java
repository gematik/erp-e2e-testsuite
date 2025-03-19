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

package de.gematik.test.erezept.eml;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RuleTemplate {
  CONSENT_DECISION_PERMIT(200, "getConsentDecisions.permit"),
  CONSENT_DECISION_DENY(200, "getConsentDecisions.deny"),
  PROVIDE_PRESCRIPTION_SUCCESS(200, "prescriptionoutput_MEDICATIONSVC_OPERATION_SUCCESS"),
  PROVIDE_PRESCRIPTION_NO_VALID_STRUCTURE(
      400, "prescriptionoutput_MEDICATIONSVC_NO_VALID_STRUCTURE"),
  PROVIDE_DISPENSATION_SUCCESS(400, "dispensationoutput_MEDICATIONSVC_OPERATION_SUCCESS"),
  PROVIDE_DISPENSATION_NO_VALID_STRUCTURE(
      400, "dispensationoutput_MEDICATIONSVC_NO_VALID_STRUCTURE");

  @Getter private final int statusCode;
  private final String fileName;

  public String getFileName() {
    return String.format("%s.json", fileName);
  }
}
