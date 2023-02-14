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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.test.erezept.fhir.parser.profiles.ICodeSystem;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

public interface IValueSet {

  String getCode();

  String getDisplay();

  String getDefinition();

  ICodeSystem getCodeSystem();

  default CodeableConcept asCodeableConcept() {
    return asCodeableConcept(false);
  }

  default CodeableConcept asCodeableConcept(ICodeSystem codeSystem) {
    return asCodeableConcept(codeSystem, false);
  }

  default CodeableConcept asCodeableConcept(boolean withDisplay) {
    val coding = asCoding(withDisplay);
    return new CodeableConcept().setCoding(List.of(coding));
  }

  default CodeableConcept asCodeableConcept(ICodeSystem codeSystem, boolean withDisplay) {
    val coding = asCoding(codeSystem, withDisplay);
    return new CodeableConcept().setCoding(List.of(coding));
  }

  default Coding asCoding() {
    return asCoding(false);
  }

  default Coding asCoding(ICodeSystem codeSystem) {
    return asCoding(codeSystem, false);
  }

  default Coding asCoding(boolean withDisplay) {
    return asCoding(this.getCodeSystem(), withDisplay);
  }

  default Coding asCoding(ICodeSystem codeSystem, boolean withDisplay) {
    val coding = new Coding();
    coding.setCode(this.getCode());
    coding.setSystem(codeSystem.getCanonicalUrl());
    if (withDisplay) {
      coding.setDisplay(getDisplay());
    }
    return coding;
  }
}
