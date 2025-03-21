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
 */

package de.gematik.test.erezept.fhir.values;

import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class TaskId {

  @Getter private final String value;

  private TaskId(String value) {
    this.value = value;
  }

  public PrescriptionId toPrescriptionId() {
    return PrescriptionId.from(this.value);
  }

  public PrescriptionFlowType getFlowType() {
    return this.toPrescriptionId().getFlowType();
  }

  @Override
  public String toString() {
    return this.value;
  }

  public static TaskId from(String value) {
    return new TaskId(value.trim());
  }

  public static TaskId from(PrescriptionId prescriptionId) {
    return from(prescriptionId.getValue());
  }
}
