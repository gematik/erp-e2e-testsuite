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

package de.gematik.test.erezept.fhir.builder;

import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import lombok.val;
import org.hl7.fhir.r4.model.*;

// TODO: get rid of this class and move remaining methods to separate extension packages
/** Utility Class provides convenient way for creating predefined FHIR Types/Extension etc. */
public class BuilderUtil {

  private BuilderUtil() {
    throw new AssertionError();
  }

  public static Extension dosageFlag(boolean value) {
    return new Extension(
        ErpStructureDefinition.KBV_DOSAGE_FLAG.getCanonicalUrl(), new BooleanType(value));
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
    return new Extension(ErpStructureDefinition.KBV_BVG.getCanonicalUrl(), new BooleanType(value));
  }

  public static Extension hasEmergencyServiceFee(boolean value) {
    return new Extension(
        ErpStructureDefinition.KBV_EMERGENCY_SERVICES_FEE.getCanonicalUrl(),
        new BooleanType(value));
  }

  public static Extension vaccine(boolean value) {
    return new Extension(
        ErpStructureDefinition.KBV_MEDICATION_VACCINE.getCanonicalUrl(), new BooleanType(value));
  }

  public static Extension markingFlag(
      boolean insuranceProvider, boolean subsidy, boolean taxOffice) {
    val markingFlag = new Extension(ErpStructureDefinition.GEM_MARKING_FLAG.getCanonicalUrl());

    val extInsurance = new Extension("insuranceProvider", new BooleanType(insuranceProvider));
    val extSubsidy = new Extension("subsidy", new BooleanType(subsidy));
    val extTaxOffice = new Extension("taxOffice", new BooleanType(taxOffice));
    markingFlag.addExtension(extInsurance).addExtension(extSubsidy).addExtension(extTaxOffice);
    return markingFlag;
  }

  public static Quantity packageQuantity(int amount) {
    val q = new Quantity();
    q.setSystem(ErpCodeSystem.UCUM.getCanonicalUrl());
    q.setCode("{Package}");
    q.setValue(amount);
    return q;
  }

  public static CodeableConcept dataAbsent() {
    return dataAbsent("not-applicable");
  }

  public static CodeableConcept dataAbsent(String value) {
    val coding = new Coding();
    coding.setSystem(ErpCodeSystem.DATA_ABSENT.getCanonicalUrl()).setCode(value);
    return new CodeableConcept(coding);
  }

  public static CodeableConcept kbvSectionType(String code) {
    val coding = new Coding();
    coding.setSystem(ErpCodeSystem.SECTION_TYPE.getCanonicalUrl());
    coding.setCode(code);
    return new CodeableConcept(coding);
  }
}
