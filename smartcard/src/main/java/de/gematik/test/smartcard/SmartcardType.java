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

package de.gematik.test.smartcard;

import de.gematik.test.smartcard.exceptions.InvalidSmartcardTypeException;
import lombok.NonNull;

public enum SmartcardType {
  EGK("eGK"),
  HBA("HBA"),
  SMC_B("SMC-B");

  private final String name;

  SmartcardType(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return this.name;
  }

  public static SmartcardType fromString(@NonNull String type) {
    return switch (type.toLowerCase().strip().replace("-", "")) {
      case "egk" -> EGK;
      case "hba" -> HBA;
      case "smcb" -> SMC_B;
      default -> throw new InvalidSmartcardTypeException(type);
    };
  }
}
