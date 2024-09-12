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

package de.gematik.test.core.expectations.verifier.rawhttpverifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.actions.rawhttpactions.pki.PKICertificatesDTOEnvelop;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.val;

@SuppressWarnings("java:S1452")
public class RawHttpRespBodyCertVerifier {

  private RawHttpRespBodyCertVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<PKICertificatesDTOEnvelop> bodyContainsAddRoots(
      Set<X509Certificate> expected) {
    return bodyContainsCert(PKICertificatesDTOEnvelop::getAddRoots, expected, " add_roots");
  }

  public static VerificationStep<PKICertificatesDTOEnvelop> bodyContainsCaCerts(
      Set<X509Certificate> expected) {
    return bodyContainsCert(PKICertificatesDTOEnvelop::getCaCerts, expected, "ca_cert");
  }

  private static VerificationStep<PKICertificatesDTOEnvelop> bodyContainsCert(
      Function<PKICertificatesDTOEnvelop, Set<X509Certificate>> certFunction,
      Set<X509Certificate> expected,
      String objectUnderTest) {
    Predicate<PKICertificatesDTOEnvelop> predicate =
        body ->
            certFunction.apply(body).containsAll(expected)
                && expected.containsAll(certFunction.apply(body));

    val step =
        new VerificationStep.StepBuilder<PKICertificatesDTOEnvelop>(
            ErpAfos.A_24466.getRequirement(),
            format("Es ist im Body der Key: {0} enthalten", objectUnderTest));
    return step.predicate(predicate).accept();
  }
}
