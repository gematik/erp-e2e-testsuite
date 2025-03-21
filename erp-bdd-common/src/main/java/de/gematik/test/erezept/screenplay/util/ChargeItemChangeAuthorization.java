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

package de.gematik.test.erezept.screenplay.util;

import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChargeItemChangeAuthorization {

  private final ErxChargeItem chargeItem;
  private final PrescriptionId prescriptionId;
  private final AccessCode accessCode;

  public static ChargeItemChangeAuthorization forChargeItem(
      ErxChargeItem chargeItem, AccessCode accessCode) {
    return new ChargeItemChangeAuthorization(
        chargeItem, chargeItem.getPrescriptionId(), accessCode);
  }
}
