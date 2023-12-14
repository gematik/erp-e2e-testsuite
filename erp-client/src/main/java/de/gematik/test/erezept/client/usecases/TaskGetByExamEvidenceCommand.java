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

import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.rest.param.SortParameter;

public class TaskGetByExamEvidenceCommand extends TaskGetCommand {

  /** Get all Tasks without any sorting or filtering */
  public TaskGetByExamEvidenceCommand(String examEvidence) {
    queryParameters.add(new SortParameter("authored-on", SortOrder.DESCENDING));
    if (examEvidence != null) {
      queryParameters.add(new QueryParameter("pnw", examEvidence));
    }
  }
}
