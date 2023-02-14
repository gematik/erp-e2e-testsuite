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

package de.gematik.test.erezept.actions;

import de.gematik.test.erezept.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import lombok.*;
import net.serenitybdd.screenplay.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DownloadOpenTask extends ErpAction<ErxTaskBundle> {

  private final TaskGetCommand cmd;

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public ErpInteraction<ErxTaskBundle> answeredBy(Actor actor) {
    return this.performCommandAs(cmd, actor);
  }

  public static class Builder {

    private String examEvidence;
    private String kvnr;

    public Builder examEvidence(String examEvidence) {
      this.examEvidence = examEvidence;
      return this;
    }

    public Builder kvnr(String kvnr) {
      this.kvnr = kvnr;
      return this;
    }

    public DownloadOpenTask build() {
      val cmd = new TaskGetByExamEvidenceCommand(kvnr, examEvidence);
      return new DownloadOpenTask(cmd);
    }
  }
}
