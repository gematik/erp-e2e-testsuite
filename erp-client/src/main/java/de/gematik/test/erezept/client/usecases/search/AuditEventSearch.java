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

package de.gematik.test.erezept.client.usecases.search;

import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.rest.param.SortParameter;
import de.gematik.test.erezept.client.usecases.AuditEventGetByIdCommand;
import de.gematik.test.erezept.client.usecases.AuditEventGetCommand;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.ArrayList;
import lombok.val;

public class AuditEventSearch {

  private AuditEventSearch() {
    throw new AssertionError();
  }

  public static AuditEventGetCommand getAuditEvents() {
    return getAuditEvents(SortOrder.DESCENDING);
  }

  public static AuditEventGetCommand getAuditEvents(SortOrder order) {
    val searchParams = new ArrayList<IQueryParameter>();
    searchParams.add(new SortParameter("date", order));
    return new AuditEventGetCommand(searchParams);
  }

  public static AuditEventGetByIdCommand getAuditEventsFor(PrescriptionId prescriptionId) {
    return new AuditEventGetByIdCommand(prescriptionId);
  }
}
