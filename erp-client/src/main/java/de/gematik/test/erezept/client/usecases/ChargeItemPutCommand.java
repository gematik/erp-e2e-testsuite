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

package de.gematik.test.erezept.client.usecases;

import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.rest.param.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.values.*;
import java.util.*;
import org.hl7.fhir.r4.model.*;

public class ChargeItemPutCommand extends BaseCommand<ErxChargeItem> {

  private final ErxChargeItem body;

  public ChargeItemPutCommand(AccessCode accessCode, ErxChargeItem body) {
    this(body.getPrescriptionId(), accessCode, body);
  }

  public ChargeItemPutCommand(
      PrescriptionId prescriptionId, AccessCode accessCode, ErxChargeItem body) {
    super(ErxChargeItem.class, HttpRequestMethod.PUT, "ChargeItem", prescriptionId.getValue());
    this.body = body;
    this.queryParameters.add(new QueryParameter("ac", accessCode.getValue()));
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.of(body);
  }
}
