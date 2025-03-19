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
 */

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class VerifyDocumentResponse implements Question<Boolean> {

  private final byte[] toVerify;

  public static VerifyDocumentResponse forGivenDocument(byte[] document) {
    Object[] properties = {document};
    return Instrumented.instanceOf(VerifyDocumentResponse.class).withProperties(properties);
  }

  @Override
  @Step("{0} verifiziert das Dokument mit dem Konnektor")
  public Boolean answeredBy(Actor actor) {
    val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);
    val isValid = konnektor.verifyDocument(toVerify);
    return isValid.getPayload();
  }
}
