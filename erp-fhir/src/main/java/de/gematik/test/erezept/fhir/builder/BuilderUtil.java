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

package de.gematik.test.erezept.fhir.builder;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.Hl7CodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Quantity;

// TODO: get rid of this class and move remaining methods to separate extension packages
/** Utility Class provides convenient way for creating predefined FHIR Types/Extension etc. */
public class BuilderUtil {

  private BuilderUtil() {
    throw new AssertionError();
  }

  public static Extension dosageFlag(boolean value) {
    return new Extension(KbvItaErpStructDef.DOSAGE_FLAG.getCanonicalUrl(), new BooleanType(value));
  }

  /**
   * BVG stands for Bundesversorgungsgesetz
   *
   * @see <a href="https://www.kbv.de/media/sp/04_Krankenversichertenkarte.pdf">KBV
   *     04_Krankenversichertenkarte.pdf</a>
   * @param value true if an E-Prescription shall be BVG
   * @return Extension
   */
  public static Extension isBVG(boolean value) {
    return new Extension(KbvItaErpStructDef.BVG.getCanonicalUrl(), new BooleanType(value));
  }

  public static Extension hasEmergencyServiceFee(boolean value) {
    return new Extension(
        KbvItaErpStructDef.EMERGENCY_SERVICES_FEE.getCanonicalUrl(), new BooleanType(value));
  }

  public static Extension vaccine(boolean value) {
    return new Extension(
        KbvItaErpStructDef.MEDICATION_VACCINE.getCanonicalUrl(), new BooleanType(value));
  }

  public static Quantity packageQuantity(int amount) {
    val q = new Quantity();
    q.setSystem(CommonCodeSystem.UCUM.getCanonicalUrl());
    q.setCode("{Package}");
    q.setValue(amount);
    return q;
  }

  public static CodeableConcept dataAbsent() {
    return dataAbsent("not-applicable");
  }

  public static CodeableConcept dataAbsent(String value) {
    val coding = new Coding();
    coding.setSystem(Hl7CodeSystem.DATA_ABSENT.getCanonicalUrl()).setCode(value);
    return new CodeableConcept(coding);
  }

  public static CodeableConcept kbvSectionType(String code) {
    val coding = new Coding();
    coding.setSystem(KbvCodeSystem.SECTION_TYPE.getCanonicalUrl());
    coding.setCode(code);
    return new CodeableConcept(coding);
  }
}
