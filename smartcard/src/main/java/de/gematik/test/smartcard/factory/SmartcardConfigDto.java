/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.smartcard.factory;

import de.gematik.test.smartcard.Crypto;
import de.gematik.test.smartcard.KeystoreType;
import de.gematik.test.smartcard.SmartcardType;
import java.nio.file.Path;
import lombok.Data;

@Data
public class SmartcardConfigDto {

  private String auth;
  private String iccsn;
  private String algo;
  private String cardType;
  private String password;

  public SmartcardType getCardType() {
    return SmartcardType.fromString(cardType);
  }

  /**
   * Path to the File which contains crypto material for Authentication
   *
   * @return absolute path to the file
   */
  public Path getAuthenticatePath() {
    return Path.of(auth).toAbsolutePath();
  }

  public Crypto getAlgorithm() {
    return Crypto.fromString(algo);
  }

  /** @return always p12 for now but might be changed in the future */
  public KeystoreType getKeystoreType() {
    return KeystoreType.P12;
  }
}
