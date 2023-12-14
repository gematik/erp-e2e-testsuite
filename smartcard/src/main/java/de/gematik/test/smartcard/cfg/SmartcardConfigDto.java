/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.smartcard.cfg;

import de.gematik.test.smartcard.*;
import java.util.*;
import lombok.*;

@Data
public class SmartcardConfigDto {

  private String iccsn;

  private String name;
  private String type;

  private List<String> stores;

  private String kvnr;

  public SmartcardType getCardType() {
    return SmartcardType.fromString(type);
  }

  /**
   * @return always p12 for now but might be changed in the future
   */
  public KeystoreType getKeystoreType() {
    return KeystoreType.P12;
  }
}
