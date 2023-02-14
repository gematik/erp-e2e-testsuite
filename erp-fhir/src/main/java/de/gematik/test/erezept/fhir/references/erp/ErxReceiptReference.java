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

package de.gematik.test.erezept.fhir.references.erp;

import ca.uhn.fhir.model.api.annotation.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import org.hl7.fhir.r4.model.*;

@DatatypeDef(name = "Reference")
@SuppressWarnings({"java:S110"})
public class ErxReceiptReference extends Reference {

  private static final ErpWorkflowStructDef STRUCTURE_DEFINITION =
      ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE;

  public ErxReceiptReference(ErxReceipt erxReceipt) {
    this.setDisplay(STRUCTURE_DEFINITION.getCanonicalUrl()).setReference(erxReceipt.getId());
  }
}
