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

package de.gematik.test.erezept.client.usecases;

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class MedicationDispenseGetCommand extends BaseCommand<ErxMedicationDispense> {

  public MedicationDispenseGetCommand() {
    super(ErxMedicationDispense.class, HttpRequestMethod.GET, "/MedicationDispense");
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.empty();
  }
}
