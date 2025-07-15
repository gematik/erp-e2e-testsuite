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

package de.gematik.test.erezept.eml;

import lombok.Getter;

@Getter
public enum RuleIdentifier {
  PROVIDE_PRESCRIPTION("providePrescription"),
  CANCEL_PRESCRIPTION("cancelPrescription"),
  PROVIDE_DISPENSATION("provideDispensation"),
  CANCEL_DISPENSATION("cancelDispensation"),
  CONSENT_DECISION("getConsentDecisions");

  private final String urlname;

  RuleIdentifier(String urlname) {
    this.urlname = urlname;
  }
}
