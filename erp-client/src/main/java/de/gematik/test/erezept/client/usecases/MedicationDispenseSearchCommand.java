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

package de.gematik.test.erezept.client.usecases;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

public class MedicationDispenseSearchCommand extends BaseCommand<ErxMedicationDispenseBundle> {

  public MedicationDispenseSearchCommand(PrescriptionId prescriptionId) {
    super(ErxMedicationDispenseBundle.class, HttpRequestMethod.GET, "/MedicationDispense");

    // the format of the identifier is described in A_22070
    val identifier =
        format("{0}|{1}", prescriptionId.getSystemAsString(), prescriptionId.getValue());
    this.queryParameters.add(new QueryParameter("identifier", identifier));
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.empty();
  }
}
