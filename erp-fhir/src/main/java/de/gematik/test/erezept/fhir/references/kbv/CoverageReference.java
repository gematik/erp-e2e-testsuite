/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.references.kbv;

import de.gematik.test.erezept.fhir.references.CustomReferenceProvider;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Reference;

public class CoverageReference extends CustomReferenceProvider {

  private String display;
  
  public CoverageReference(String referenceValue) {
    super("Coverage", referenceValue);
  }

  public CoverageReference(Coverage coverage) {
    this(coverage.getId());
    this.display = coverage.getPayorFirstRep().getDisplay();
  }

  @Override
  public Reference asReference() {
    return new Reference(referenceValue).setDisplay(this.display);
  }
}
