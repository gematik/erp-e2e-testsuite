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

package de.gematik.test.core.expectations.rawhttpverifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.actions.rawhttpactions.dto.PKICertificatesDTOEnvelop;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.val;

@SuppressWarnings("java:S1452")
public class RawHttpRespBodyCertVerifier {

  private RawHttpRespBodyCertVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<PKICertificatesDTOEnvelop> bodyContainsAddRoots() {
    return bodyContainsCert(PKICertificatesDTOEnvelop::getAddRoots, " add_roots");
  }

  public static VerificationStep<PKICertificatesDTOEnvelop> bodyContainsCaCerts() {
    return bodyContainsCert(PKICertificatesDTOEnvelop::getCaCerts, "ca_cert");
  }

  private static VerificationStep<PKICertificatesDTOEnvelop> bodyContainsCert(
      Function<PKICertificatesDTOEnvelop, List<String>> certFunction, String objectUnderTest) {
    Predicate<PKICertificatesDTOEnvelop> predicate = body -> !certFunction.apply(body).isEmpty();
    val step =
        new VerificationStep.StepBuilder<PKICertificatesDTOEnvelop>(
            ErpAfos.A_24466.getRequirement(),
            format("Es ist im Body der Key: {0} enthalten", objectUnderTest));
    return step.predicate(predicate).accept();
  }
}
