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

package de.gematik.test.erezept.app.abilities;

import static java.text.MessageFormat.format;

import com.github.javafaker.Faker;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Ability;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HandleAppAuthentication implements Ability {

  @Getter private final String password;

  public static HandleAppAuthentication withGivenPassword(@NonNull final String password) {
    log.info(format("Use Password to protect the App: {0}", password));
    return new HandleAppAuthentication(password);
  }

  public static HandleAppAuthentication withStrongPassword() {
    val faker = new Faker();
    val password = faker.regexify("[ -~]{15}");
    return withGivenPassword(password);
  }

  @Override
  public String toString() {
    return format("use and provide an App Password: {0}", password);
  }
}
