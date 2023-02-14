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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.usecases.ConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.ConsentGetCommand;
import de.gematik.test.erezept.client.usecases.ConsentPostCommand;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.NonNull;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class BillingInformationConsent implements Task {

  private final ConsentAction action;

  private BillingInformationConsent(ConsentAction action) {
    this.action = action;
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val patientData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);

    switch (action) {
      case GRANT:
        grantConsentOnFD(erpClientAbility, patientData);
        break;
      case GET:
        readConsentOnFD(erpClientAbility, patientData);
        break;
      case REVOKE:
        deleteConsentOnFD(erpClientAbility, patientData);
        break;
    }
  }

  private void grantConsentOnFD(UseTheErpClient erpClient, ProvidePatientBaseData baseData) {
    val cmd = new ConsentPostCommand(baseData.getKvid());
    val response = erpClient.request(cmd);

    // Note: this will ensure we've received the correct response from FD
    val erxConsent = response.getResource(cmd.expectedResponseBody());
    baseData.setErxConsent(erxConsent);
  }

  private void readConsentOnFD(UseTheErpClient erpClient, ProvidePatientBaseData baseData) {
    val cmd = new ConsentGetCommand();
    val response = erpClient.request(cmd);

    val erxConsent = response.getResource(cmd.expectedResponseBody());
    baseData.setErxConsent(erxConsent);
  }

  private void deleteConsentOnFD(UseTheErpClient erpClient, ProvidePatientBaseData baseData) {
    readConsentOnFD(erpClient, baseData);
    if (baseData.hasRememberedConsent()) {
      val erxConsentId = baseData.getRememberedConsent().orElseThrow().getId();
      val cmd = new ConsentDeleteCommand(erxConsentId);

      val response = erpClient.request(cmd); // NOSONAR request is required, the response isn't yet!
      // TODO: we should also assert here something!! // NOSONAR behaviour not fully defined yet
    }
  }

  public static BillingInformationConsent grantConsent() {
    return forAction(ConsentAction.GRANT);
  }

  public static BillingInformationConsent revokeConsent() {
    return forAction(ConsentAction.REVOKE);
  }

  public static BillingInformationConsent getConsent() {
    return forAction(ConsentAction.GET);
  }

  public static BillingInformationConsent forAction(String actionName) {
    return forAction(ConsentAction.fromString(actionName));
  }

  public static BillingInformationConsent forAction(ConsentAction action) {
    return new BillingInformationConsent(action);
  }

  private enum ConsentAction {
    GRANT,
    REVOKE,
    GET;

    public static ConsentAction fromString(@NonNull final String action) {
      ConsentAction ret;
      switch (action.toLowerCase()) {
        case "erteilt":
        case "erteilen":
          ret = ConsentAction.GRANT;
          break;
        case "zurückgezogen":
        case "widerrufen":
        case "zurückziehen":
        case "zurückzieht":
        case "widerruft":
          ret = ConsentAction.REVOKE;
          break;
        case "abrufen":
        case "abgerufen":
          ret = ConsentAction.GET;
          break;
        default:
          throw new UnsupportedOperationException(
              format("{0} is an invalid action for Consent", action));
      }
      return ret;
    }
  }
}
