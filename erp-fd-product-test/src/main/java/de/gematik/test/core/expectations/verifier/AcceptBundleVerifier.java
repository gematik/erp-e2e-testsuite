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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import java.util.function.Predicate;
import lombok.val;
import org.hl7.fhir.r4.model.Task;

public class AcceptBundleVerifier {

  private AcceptBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxAcceptBundle> isInProgressStatus() {
    Predicate<ErxAcceptBundle> predicate =
        acceptBundle -> Task.TaskStatus.INPROGRESS.equals(acceptBundle.getTask().getStatus());
    val step =
        new VerificationStep.StepBuilder<ErxAcceptBundle>(
            ErpAfos.A_19169,
            format(
                "Der Status eines neuen Tasks muss sich im Status {0} befinden",
                Task.TaskStatus.INPROGRESS));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxAcceptBundle> consentIsPresent(boolean isPresent) {
    Predicate<ErxAcceptBundle> predicate =
            acceptBundle -> acceptBundle.hasConsent() == isPresent;
    
    String expectation;
    if (isPresent) {
      expectation = "Bei vorhandener Einwilligung des Versicherten muss diese im AcceptBundle vorhanden sein";
    } else {
      expectation = "Bei nicht vorhandener Einwilligung des Versicherten darf diese im AcceptBundle nicht vorhanden sein";
    }
    
    val step = new VerificationStep.StepBuilder<ErxAcceptBundle>(ErpAfos.A_22110, expectation);
    return step.predicate(predicate).accept();
  }
}
