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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.r4.erp.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.util.*;
import lombok.*;
import net.serenitybdd.screenplay.*;

public class BillingInformationConsent implements Task {

  private final ConsentAction action;

  private BillingInformationConsent(ConsentAction action) {
    this.action = action;
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

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val patientData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);

    switch (action) {
      case GRANT -> grantConsentOnFD(erpClientAbility, patientData);
      case GET -> readConsentOnFD(erpClientAbility, patientData);
      case REVOKE -> deleteConsentOnFD(erpClientAbility, patientData);
    }
  }

  private void grantConsentOnFD(UseTheErpClient erpClient, ProvidePatientBaseData baseData) {
    val optionalConsent = this.getConsent(erpClient);
    optionalConsent.ifPresentOrElse(
        baseData::setErxConsent,
        () -> {
          val cmd = new ConsentPostCommand(baseData.getKvnr());
          val response = erpClient.request(cmd);
          val erxConsent = response.getExpectedResource();
          baseData.setErxConsent(erxConsent);
        });
  }

  private void readConsentOnFD(UseTheErpClient erpClient, ProvidePatientBaseData baseData) {
    val optionalConsent = this.getConsent(erpClient);
    optionalConsent.ifPresent(baseData::setErxConsent);
  }

  private void deleteConsentOnFD(UseTheErpClient erpClient, ProvidePatientBaseData baseData) {
    readConsentOnFD(erpClient, baseData);
    baseData
        .getRememberedConsent()
        .ifPresent(
            erxConsent -> {
              val cmd = new ConsentDeleteCommand();
              val response = erpClient.request(cmd);
              assertTrue(response.getStatusCode() < 300);
              baseData.setErxConsent(null); // also forget the given consent locally
            });
  }

  private Optional<ErxConsent> getConsent(UseTheErpClient erpClient) {
    val response = erpClient.request(new ConsentGetCommand());
    val consentBundle = response.getResourceOptional();

    return consentBundle
        .filter(ErxConsentBundle::hasConsent)
        .filter(cb -> cb.getConsent().isPresent())
        .flatMap(ErxConsentBundle::getConsent);
  }

  public enum ConsentAction {
    GRANT,
    REVOKE,
    GET;

    public static ConsentAction fromString(@NonNull final String action) {
      return switch (action.toLowerCase()) {
        case "erteilt", "erteilen" -> ConsentAction.GRANT;
        case "zurückgezogen",
            "widerrufen",
            "zurückziehen",
            "zurückzieht",
            "widerruft" -> ConsentAction.REVOKE;
        case "abrufen", "abgerufen" -> ConsentAction.GET;
        default -> throw new UnsupportedOperationException(
            format("{0} is an invalid action for Consent", action));
      };
    }
  }
}
