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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.ensure.Ensure;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;

public class HasDownloadableOpenTask implements Question<Boolean> {
  private final String examEvidence;

  private HasDownloadableOpenTask() {
    this(null);
  }

  private HasDownloadableOpenTask(String examEvidence) {
    this.examEvidence = examEvidence;
  }

  private void performSignatureChecks(Actor actor, ErxTaskBundle erxTaskBundle) {
    // A_22431: ensure that erxTaskBundle do not included signatures
    actor.attemptsTo(Ensure.that(erxTaskBundle.getSignature() != null).isTrue());

    // Ensure that all contained resource are instances of ErxTask
    actor.attemptsTo(Ensure.that(
            erxTaskBundle.getEntry().stream()
                .map(BundleEntryComponent::getResource)
                .anyMatch(r -> r instanceof ErxTask))
            .isTrue());

    // Ensure that all contained Tasks do not contained resources like a binary resource for a
    // signature
    actor.attemptsTo(Ensure.that(
            erxTaskBundle.getEntry().stream()
                .map(e -> (DomainResource) e.getResource())
                .anyMatch(r -> r.getContained().isEmpty()))
        .isTrue());
  }

  @Override
  public Boolean answeredBy(Actor actor) {
    val response = actor.asksFor(ResponseOfGetTask.asPharmacy(examEvidence));
    if (response.isOperationOutcome()) {
      actor.attemptsTo(Ensure.that(response.getStatusCode()).isGreaterThanOrEqualTo(400));
      return false;
    } else {
      actor.attemptsTo(Ensure.that(response.getStatusCode()).isEqualTo(200));
      performSignatureChecks(actor, response.getExpectedResource());
      return true;
    }
  }

  public static HasDownloadableOpenTask withExamEvidence(String examEvidence) {
    return new HasDownloadableOpenTask(examEvidence);
  }

  public static HasDownloadableOpenTask withoutExamEvidence() {
    return new HasDownloadableOpenTask();
  }
}
