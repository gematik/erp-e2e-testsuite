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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@RequiredArgsConstructor
public class TheFhirDocumentSigner<R extends Resource> implements Question<Function<R, byte[]>> {

  private final Function<R, String> encoder;
  private final SmartcardType smartcardType;

  @Override
  public Function<R, byte[]> answeredBy(Actor actor) {
    val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);

    return switch (this.smartcardType) {
      case HBA -> (b) -> {
        val encoded = encoder.apply(b);
        return konnektor.signDocumentWithHba(encoded).getPayload();
      };
      case SMC_B -> (b) -> {
        val encoded = encoder.apply(b);
        return konnektor.signDocumentWithSmcb(encoded).getPayload();
      };
      default -> {
        // this case should not happen anyway
        log.warn("Trying to sign document with {}", this.smartcardType);
        yield (b) -> b.getClass().getSimpleName().getBytes(StandardCharsets.UTF_8);
      }
    };
  }

  public static <R extends Resource> Builder<R> withEncoder(Function<R, String> encoder) {
    return new Builder<>(encoder);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder<R extends Resource> {
    private final Function<R, String> encoder;

    public TheFhirDocumentSigner<R> usingHba() {
      return using(SmartcardType.HBA);
    }

    public TheFhirDocumentSigner<R> usingSmcb() {
      return using(SmartcardType.SMC_B);
    }

    public TheFhirDocumentSigner<R> using(SmartcardType smartcardType) {
      return new TheFhirDocumentSigner<>(encoder, smartcardType);
    }
  }
}
