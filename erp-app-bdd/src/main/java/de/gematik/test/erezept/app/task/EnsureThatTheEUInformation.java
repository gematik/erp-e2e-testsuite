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

package de.gematik.test.erezept.app.task;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.EUFlowElements;
import de.gematik.test.erezept.client.usecases.eu.EuGrantAccessGetCommand;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class EnsureThatTheEUInformation implements Task {
  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val baseData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val actualKVNR = app.getText(EUFlowElements.KVNR_TEXT_FIELD);
    // Note: The app inserts whitespace between the chars for aesthetic reasons, which we filter
    val actualEUAccessCode =
        app.getText(EUFlowElements.EU_ACCESS_CODE_TEXT_FIELD).replaceAll("\\s+", "");

    val expectedKVNR = baseData.getKvnr().getValue();

    val euGrantAccessGetCommand = new EuGrantAccessGetCommand();
    val erpResponse = erpClient.request(euGrantAccessGetCommand);
    val expectedEUAccessCode = erpResponse.getExpectedResource().getAccessCode().getValue();

    assertEquals(
        expectedKVNR,
        actualKVNR,
        format(
            "Displayed KVNR is not equal to expected KVNR. Expected: {0}, Actual: {1}",
            expectedKVNR, actualKVNR));
    assertEquals(
        expectedEUAccessCode,
        actualEUAccessCode,
        format(
            "Displayed EU Access Code is not equal to expected EU Access Code. Expected: {0},"
                + " Actual: {1}",
            expectedEUAccessCode, actualEUAccessCode));
  }

  public static EnsureThatTheEUInformation isDisplayedCorrectly() {
    return new EnsureThatTheEUInformation();
  }
}
