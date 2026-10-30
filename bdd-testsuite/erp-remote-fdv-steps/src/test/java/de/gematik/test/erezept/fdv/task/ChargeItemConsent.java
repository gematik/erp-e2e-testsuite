/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fdv.task;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.erezept.remotefdv.api.model.Consent;
import de.gematik.erezept.remotefdv.api.model.ConsentCategory;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.*;
import java.util.*;
import lombok.*;
import net.serenitybdd.screenplay.*;

public class ChargeItemConsent implements Task {

  private final ConsentAction action;

  private ChargeItemConsent(ConsentAction action) {
    this.action = action;
  }

  public static ChargeItemConsent grantConsent() {
    return forAction(ConsentAction.GRANT);
  }

  public static ChargeItemConsent revokeConsent() {
    return forAction(ConsentAction.REVOKE);
  }

  public static ChargeItemConsent getConsent() {
    return forAction(ConsentAction.GET);
  }

  private static ChargeItemConsent forAction(ConsentAction action) {
    return new ChargeItemConsent(action);
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val remoteFdVClientAbility = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    val provideEGKAbility = SafeAbility.getAbility(actor, ProvideEGK.class);
    val kvnr = provideEGKAbility.getKvnr();

    switch (action) {
      case GRANT -> grantConsent(remoteFdVClientAbility, kvnr);
      case GET -> readConsent(remoteFdVClientAbility, kvnr);
      case REVOKE -> deleteConsent(remoteFdVClientAbility);
    }
  }

  private void grantConsent(UseTheRemoteFdVClient client, KVNR kvnr) {
    val optionalConsent = this.getConsent(client);
    optionalConsent.ifPresentOrElse(
        consent ->
            assertEquals(
                kvnr.getValue(),
                consent.getKvnr(),
                "KVNR of existing consent does not match the KVNR of the current patient"),
        () -> {
          val response = client.sendRequest(PatientRequests.postConsent(ConsentCategory.CHARGCONS));
          val newConsent = assertDoesNotThrow(response::getExpectedResource);
          assertEquals(ConsentCategory.CHARGCONS, newConsent.getCategory());
          assertEquals(kvnr.getValue(), newConsent.getKvnr());
        });
  }

  private void readConsent(UseTheRemoteFdVClient client, KVNR kvnr) {
    val optionalConsent = this.getConsent(client);
    optionalConsent.ifPresent(
        consent -> {
          assertEquals(ConsentCategory.CHARGCONS, consent.getCategory());
          assertEquals(kvnr.getValue(), consent.getKvnr());
        });
  }

  private void deleteConsent(UseTheRemoteFdVClient client) {
    val optionalConsent = this.getConsent(client);
    optionalConsent.ifPresent(
        consent -> {
          client.sendRequest(PatientRequests.deleteConsent(ConsentCategory.CHARGCONS));
          assertEquals(Optional.empty(), this.getConsent(client));
        });
  }

  private Optional<Consent> getConsent(UseTheRemoteFdVClient client) {
    val response = client.sendRequest(PatientRequests.getConsent(ConsentCategory.CHARGCONS));
    return response.getResourceOptional();
  }

  private enum ConsentAction {
    GRANT,
    REVOKE,
    GET;
  }
}
