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

package de.gematik.test.erezept.screenplay.util;

import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class DmcPrescription {

  private final Instant created;
  private final TaskId taskId;
  private final AccessCode accessCode;
  private final boolean representative;

  /**
   * Create a DmcPrescription (Data Matrix Code) for a "Representative Prescription"
   *
   * @param taskId is the Task-ID (FHIR-Resource ID) of the prescription
   * @param accessCode is the AccessCode of the Prescription
   * @param representative indicates if the owner of this object has this DMC as representative
   */
  private DmcPrescription(TaskId taskId, AccessCode accessCode, boolean representative) {
    this.created = Instant.now();
    this.taskId = taskId;
    this.accessCode = accessCode;
    this.representative = representative;
  }

  public static DmcPrescription ownerDmc(TaskId taskId, AccessCode accessCode) {
    return new DmcPrescription(taskId, accessCode, false);
  }

  public static DmcPrescription representativeDmc(TaskId taskId, AccessCode accessCode) {
    return new DmcPrescription(taskId, accessCode, true);
  }
}
