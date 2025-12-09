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

package de.gematik.test.erezept.actions.eu;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.usecases.eu.EuConsentPostCommand;
import de.gematik.test.erezept.fhir.r4.eu.EuConsent;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

public class GrantEuConsent extends ErpAction<EuConsent> {

  @Override
  @Step("{0} gibt eine Einwilligung (Consent) um im EU Ausland Rezepte einlösen zu können")
  public ErpInteraction<EuConsent> answeredBy(Actor actor) {
    val patient = (PatientActor) actor;

    val command = new EuConsentPostCommand(patient.getKvnr());
    return performCommandAs(command, patient);
  }

  public static GrantEuConsent forPatient() {
    return new GrantEuConsent();
  }
}
