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

package de.gematik.test.erezept.primsys.model.actor;

import de.gematik.test.erezept.primsys.exceptions.InvalidActorRoleException;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ActorRole {
  DOCTOR("Arzt"),
  PHARMACY("Apotheke");

  @Getter private final String readable;

  ActorRole(final String readable) {
    this.readable = readable;
  }

  @Override
  public String toString() {
    return readable;
  }

  public static ActorRole fromString(String value) {
    ActorRole ret;
    switch (value.toLowerCase()) {
      case "arzt":
      case "doctor":
        ret = DOCTOR;
        break;
      case "apotheke":
      case "pharmacy":
        ret = PHARMACY;
        break;
      default:
        throw new InvalidActorRoleException(value);
    }
    return ret;
  }

  public static Optional<ActorRole> optionalFromString(String value) {
    Optional<ActorRole> ret = Optional.empty();
    try {
      ret = Optional.of(fromString(value));
    } catch (RuntimeException rte) {
      log.warn(rte.getMessage());
    }
    return ret;
  }
}
