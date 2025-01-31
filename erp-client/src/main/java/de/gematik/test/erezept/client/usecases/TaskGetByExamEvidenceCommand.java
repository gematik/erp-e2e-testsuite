/*
 * Copyright 2024 gematik GmbH
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

import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.fhir.values.KVNR;
import java.util.List;
import lombok.NonNull;

public class TaskGetByExamEvidenceCommand extends TaskGetCommand {

  public TaskGetByExamEvidenceCommand() {}

  /** Get all Tasks without any sorting or filtering */
  public TaskGetByExamEvidenceCommand(@NonNull String examEvidence) {
    queryParameters.add(new QueryParameter("pnw", examEvidence));
  }

  public TaskGetByExamEvidenceCommand andKvnr(@NonNull KVNR kvnr) {
    queryParameters.add(new QueryParameter("kvnr", kvnr.getValue()));
    return this;
  }

  /**
   * get Tasks with different possible Sort- or FilterOptions
   *
   * @param queryParameter
   * @return TaskGetByExamEvidenceCommand
   */
  public TaskGetByExamEvidenceCommand andAdditionalQuery(List<IQueryParameter> queryParameter) {
    queryParameters.addAll(queryParameter);
    return this;
  }
}
