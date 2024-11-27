/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.eml.tasks;

import de.gematik.test.core.exceptions.EpaMockClientException;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmlProvidePrescriptionApply implements Task {

  private final KVNR kvnr;

  public static EmlProvidePrescriptionApply forKvnr(KVNR kvnr) {
    return new EmlProvidePrescriptionApply(kvnr);
  }

  @Step("{0} gibt sein Consent zur übertragung der Prescription in die EML für #kvnr")
  public <T extends Actor> void performAs(T actor) {
    val client = SafeAbility.getAbility(actor, UseTheEpaMockClient.class);

    val works = client.setProvidePrescriptionApply(kvnr);
    if (!works) throw new EpaMockClientException("EpaMock", "prescription provide apply");
  }
}
