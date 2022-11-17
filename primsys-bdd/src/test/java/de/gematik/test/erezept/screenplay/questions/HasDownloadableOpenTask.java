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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.fhir.resources.erp.*;
import lombok.*;
import net.serenitybdd.screenplay.*;
import net.serenitybdd.screenplay.ensure.*;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Bundle.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HasDownloadableOpenTask implements Question<Boolean> {
  private final String kvnr;
  private final String examEvidence;

  public void checkSignature(ErxTaskBundle erxTaskBundle) {
    // A_22431: ensure that erxTaskBundle do not included signatures
    Ensure.that(erxTaskBundle.getSignature() != null).isTrue();

    // Ensure that all contained resource are instances of ErxTask
    Ensure.that(
            erxTaskBundle.getEntry().stream()
                .map(BundleEntryComponent::getResource)
                .anyMatch(r -> r instanceof ErxTask))
        .isTrue();

    // Ensure that all contained Tasks do not contained resources like a binary resource for a
    // signature
    Ensure.that(
            erxTaskBundle.getEntry().stream()
                .map(e -> (DomainResource) e.getResource())
                .anyMatch(r -> r.getContained().isEmpty()))
        .isTrue();
  }

  @Override
  public Boolean answeredBy(Actor actor) {
    val response = actor.asksFor(ResponseOfGetTask.asPharmacy(kvnr, examEvidence));
    if (response.isOperationOutcome()) {
      Ensure.that(response.getStatusCode()).isGreaterThanOrEqualTo(400);
      return false;
    } else {
      Ensure.that(response.getStatusCode()).isEqualTo(200);
      val erxTaskBundle = response.getResource(ErxTaskBundle.class);
      checkSignature(erxTaskBundle);
      return erxTaskBundle.getTasks().size() > 0;
    }
  }

  public static Builder builder() {
    return new Builder();
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

    public HasDownloadableOpenTask build() {
      return new HasDownloadableOpenTask(kvnr, examEvidence);
    }
  }
}
