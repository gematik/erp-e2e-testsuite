/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.references.kbv;

import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import org.hl7.fhir.r4.model.Reference;

@DatatypeDef(name = "Reference")
@SuppressWarnings({"java:S110"})
public class MedicationRequestReference extends Reference {

  private static final String MEDICATION_REQUEST_PREFIX = "MedicationRequest/";

  public MedicationRequestReference(String referenceId) {
    super(
        referenceId.startsWith(MEDICATION_REQUEST_PREFIX)
            ? referenceId
            : MEDICATION_REQUEST_PREFIX + referenceId);
  }
}
