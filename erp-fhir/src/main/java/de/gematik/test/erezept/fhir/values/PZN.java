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
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public class PZN extends Value<String> {

  private PZN(String value) {
    super(ErpCodeSystem.PZN, value);
  }

  public Coding asCoding() {
    val coding = new Coding();
    coding.setSystem(this.getSystemAsString()).setCode(this.getValue());
    return coding;
  }

  /**
   * With this method the PZN is wrapped up as a CodeableConcept with the given drugName
   *
   * @param drugName corresponding to the PZN
   * @return CodeableConcept with PZN and drug drugName
   */
  public CodeableConcept asNamedCodeable(String drugName) {
    val codeable = new CodeableConcept(asCoding());
    codeable.setText(drugName);
    return codeable;
  }

  /**
   * With this method the PZN is wrapped up as a CodeableConcept with a randomly generated name
   *
   * @return CodeableConcept with PZN and a random drug name
   */
  public CodeableConcept asNamedCodeable() {
    return asNamedCodeable(GemFaker.fakerDrugName());
  }

  public static PZN from(@NonNull String value) {
    return new PZN(value);
  }

  public static PZN random() {
    return new PZN(GemFaker.fakerPzn());
  }
}
