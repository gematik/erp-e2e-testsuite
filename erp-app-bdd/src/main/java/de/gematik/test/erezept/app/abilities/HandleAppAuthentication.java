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

package de.gematik.test.erezept.app.abilities;

import static java.text.MessageFormat.format;

import com.github.javafaker.Faker;
import javax.annotation.Nullable;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Ability;

@Getter
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HandleAppAuthentication implements Ability {

  private final String password;

  public static HandleAppAuthentication withGivenPassword(@Nullable String password) {
    if (password != null) {
      log.info(format("Use Password to protect the App: {0}", password));
      return new HandleAppAuthentication(password);
    } else {
      return withStrongPassword();
    }
  }

  public static HandleAppAuthentication withStrongPassword() {
    val faker = new Faker();
    val password = faker.regexify("[ -~]{8}");
    return withGivenPassword(password);
  }

  @Override
  public String toString() {
    return format("use and provide an App Password: {0}", password);
  }
}
