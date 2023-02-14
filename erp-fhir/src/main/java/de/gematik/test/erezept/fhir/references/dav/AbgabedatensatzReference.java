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

package de.gematik.test.erezept.fhir.references.dav;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.DatatypeDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpPkvStructDef;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;

@DatatypeDef(name = "Reference")
@SuppressWarnings({"java:S110"})
public class AbgabedatensatzReference extends Reference {

  private static final String DISPLAY_VALUE = "Abgabedatensatz";
  private static final AbdaErpPkvStructDef STRUCTURE_DEFINITION =
      AbdaErpPkvStructDef.PKV_ABGABEDATENSATZ;

  public AbgabedatensatzReference(String id) {
    this.setDisplay(DISPLAY_VALUE).setReference(id).setType(STRUCTURE_DEFINITION.getCanonicalUrl());
  }

  public void makeContained() {
    val ref = this.getReference();
    if (!ref.startsWith("#")) {
      this.setReference(format("#{0}", ref));
    }

    this.setType("Binary");
  }

  public String getReference(boolean containedMarker) {
    val ref = this.getReference();
    if (ref.startsWith("#") && !containedMarker) {
      return ref.substring(1);
    } else {
      return ref;
    }
  }
}
