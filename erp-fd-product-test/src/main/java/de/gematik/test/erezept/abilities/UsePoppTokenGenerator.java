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

package de.gematik.test.erezept.abilities;

import de.gematik.bbriccs.popp.PoppTokenGenerator;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmcB;
import net.serenitybdd.screenplay.Ability;

public class UsePoppTokenGenerator implements Ability {

  private final PoppTokenGenerator generator;
  private final SmcB smcb;

  private UsePoppTokenGenerator(PoppTokenGenerator generator, SmcB smcb) {
    this.generator = generator;
    this.smcb = smcb;
  }

  public static UsePoppTokenGenerator with(PoppTokenGenerator generator, SmcB smcb) {
    return new UsePoppTokenGenerator(generator, smcb);
  }

  public String sign(Egk egk) {
    return generator.sign(PoppTokenGenerator.TokenGenerationRequest.with(smcb, egk));
  }

  public String signExpired(Egk egk) {
    return generator.signExpiredToken(PoppTokenGenerator.TokenGenerationRequest.with(smcb, egk));
  }

  public String signWithWrongKey(Egk egk) {
    return generator.signWithWrongKey(PoppTokenGenerator.TokenGenerationRequest.with(smcb, egk));
  }

  public String signWithInvalidKid(Egk egk) {
    var req = PoppTokenGenerator.TokenGenerationRequest.with(smcb, egk);
    req.setKid("invalidKid");
    return generator.sign(req);
  }

  public String signWithInvalidIssuer(Egk egk) {
    var req = PoppTokenGenerator.TokenGenerationRequest.with(smcb, egk);
    req.setIssuer("https://dummy.de");
    return generator.sign(req);
  }
}
