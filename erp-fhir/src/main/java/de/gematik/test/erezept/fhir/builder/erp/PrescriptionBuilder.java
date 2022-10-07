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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Parameters;

public class PrescriptionBuilder {

  private PrescriptionBuilder() {
    throw new AssertionError();
  }

  public static Parameters builder(byte[] data) {
    val b = new Binary();
    b.getMeta().setProfile(List.of(ErpStructureDefinition.GEM_BINARY.asCanonicalType()));
    b.getContentTypeElement().setValue("application/pkcs7-mime");
    b.setData(data);

    val params = new Parameters();
    val p = params.addParameter();
    p.setName("ePrescription");
    p.setResource(b);

    return params;
  }
}
