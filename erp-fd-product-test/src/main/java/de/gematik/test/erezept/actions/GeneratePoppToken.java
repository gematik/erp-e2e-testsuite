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

package de.gematik.test.erezept.actions;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.erezept.abilities.UsePoppTokenGenerator;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor
public class GeneratePoppToken implements Question<String> {

  private final Egk egk;
  private Mode mode;

  public enum Mode {
    VALID,
    EXPIRED,
    WRONG_KEY,
    INVALID_KID,
    INVALID_ISSUER
  }

  public GeneratePoppToken(Egk egk, Mode mode) {
    this.egk = egk;
    this.mode = mode;
  }

  public static GeneratePoppToken forEgk(Egk egk) {
    return new GeneratePoppToken(egk, Mode.VALID);
  }

  public GeneratePoppToken expired() {
    this.mode = Mode.EXPIRED;
    return this;
  }

  public GeneratePoppToken wrongKey() {
    this.mode = Mode.WRONG_KEY;
    return this;
  }

  public GeneratePoppToken invalidKid() {
    this.mode = Mode.INVALID_KID;
    return this;
  }

  public GeneratePoppToken invalidIssuer() {
    this.mode = Mode.INVALID_ISSUER;
    return this;
  }

  @Override
  public String answeredBy(Actor actor) {

    var generator = SafeAbility.getAbility(actor, UsePoppTokenGenerator.class);

    return switch (mode) {
      case VALID -> generator.sign(egk);
      case EXPIRED -> generator.signExpired(egk);
      case WRONG_KEY -> generator.signWithWrongKey(egk);
      case INVALID_KID -> generator.signWithInvalidKid(egk);
      case INVALID_ISSUER -> generator.signWithInvalidIssuer(egk);
    };
  }
}
