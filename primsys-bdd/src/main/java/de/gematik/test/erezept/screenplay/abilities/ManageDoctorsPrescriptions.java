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

package de.gematik.test.erezept.screenplay.abilities;

import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.screenplay.util.*;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

/**
 * This ability is intended to be used by the Doctor-Actor for managing it's issued Prescriptions
 */
@Slf4j
public class ManageDoctorsPrescriptions implements Ability {

  @Getter @Delegate private final ManagedList<ErxTask> prescriptions;

  private ManageDoctorsPrescriptions() {
    this.prescriptions = new ManagedList<>(() -> "No Prescriptions were issued so far");
  }

  public static ManageDoctorsPrescriptions sheIssued() {
    return heIssued();
  }

  public static ManageDoctorsPrescriptions heIssued() {
    return new ManageDoctorsPrescriptions();
  }

  @Override
  public String toString() {
    return "E-Rezepte Manager für die Verwaltung von ausgestellten E-Rezepten";
  }
}
