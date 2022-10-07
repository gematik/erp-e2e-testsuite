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

package de.gematik.test.erezept.fhir.values;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;

public class PrescriptionId extends Value<String> {

  public static final ErpNamingSystem NAMING_SYSTEM = ErpNamingSystem.PRESCRIPTION_ID;

  public PrescriptionId(final String value) {
    super(NAMING_SYSTEM, value);
  }

  public boolean check() {
    return checkId(this.getValue());
  }

  public static PrescriptionId random() {
    return random(PrescriptionFlowType.FLOW_TYPE_160);
  }

  public static PrescriptionId random(PrescriptionFlowType flowType) {
    return GemFaker.fakerPrescriptionId(flowType);
  }

  public static boolean checkId(PrescriptionId prescriptionId) {
    return checkId(prescriptionId.getValue());
  }

  public static boolean checkId(String value) {
    val raw = Long.parseLong(value.replace(".", ""));
    val check = raw % 97;
    return check == 1;
  }
}
