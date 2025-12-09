/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.actions;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.usecases.MedicationDispenseGetCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class GetMedicationDispense extends ErpAction<ErxMedicationDispenseBundle> {

  private final MedicationDispenseGetCommand medicationDispenseGetCommand;

  public static GetMedicationDispense whenHandedOver(SearchPrefix searchPrefix, LocalDate date) {
    return new GetMedicationDispense(
        new MedicationDispenseGetCommand(
            IQueryParameter.search().whenHandedOver(searchPrefix, date).createParameter()));
  }

  public static GetMedicationDispense whenHandedOverDefault() {
    return new GetMedicationDispense(new MedicationDispenseGetCommand());
  }

  public static GetMedicationDispense whenPrepared(SearchPrefix searchPrefix, LocalDate date) {
    return new GetMedicationDispense(
        new MedicationDispenseGetCommand(
            IQueryParameter.search().whenPrepared(searchPrefix, date).createParameter()));
  }

  public static GetMedicationDispense withCount(int count) {
    return new GetMedicationDispense(
        new MedicationDispenseGetCommand(
            IQueryParameter.search().withCount(count).createParameter()));
  }

  public static GetMedicationDispense withQueryParams(List<IQueryParameter> queryParameter) {
    return new GetMedicationDispense(new MedicationDispenseGetCommand(queryParameter));
  }

  public static GetMedicationDispense fromPerformer(TelematikID telematikId) {
    return new GetMedicationDispense(
        new MedicationDispenseGetCommand(
            IQueryParameter.search().fromPerformer(telematikId).createParameter()));
  }

  @Override
  @Step("{0} ruft MedicationDispenses beim FD ab")
  public ErpInteraction<ErxMedicationDispenseBundle> answeredBy(Actor actor) {
    return performCommandAs(medicationDispenseGetCommand, actor);
  }
}
