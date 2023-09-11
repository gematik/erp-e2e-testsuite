/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.konnektor.soap.mock.vsdm;

import lombok.*;

@RequiredArgsConstructor
public enum VsdmExamEvidenceResult {
  UPDATES_SUCCESSFUL(1),
  NO_UPDATES(2),
  ERROR_EGK(3),
  ERROR_AUTH_CERT_INVALID(4),
  ERROR_ONLINECHECK_NOT_POSSIBLE(5),
  ERROR_OFFLINE_PERIOD_EXCEEDED(6),
  INVALID_EVIDENCE_RESULT(-1);

  @Getter private final int result;
}
