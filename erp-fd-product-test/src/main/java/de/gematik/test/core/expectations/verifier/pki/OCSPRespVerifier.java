/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.core.expectations.verifier.pki;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.actions.rawhttpactions.pki.*;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.konnektor.soap.mock.LocalVerifier;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPToken;
import java.util.Arrays;
import java.util.function.Predicate;
import lombok.val;
import org.bouncycastle.asn1.ocsp.OCSPResponseStatus;

public class OCSPRespVerifier {
  private OCSPRespVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<OCSPBodyWrapper> ocspResponseStatusIsGood() {
    Predicate<OCSPBodyWrapper> predicate =
        bW -> bW.getStatus().getIntValue() == OCSPResponseStatus.SUCCESSFUL;
    val step =
        new VerificationStep.StepBuilder<OCSPBodyWrapper>(
            ErpAfos.A_24467.getRequirement(),
            "Der OcspResponse hat den CertStatus NULL oder Successful");
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<OCSPBodyWrapper> responseContainsDescription(String description) {
    Predicate<OCSPBodyWrapper> predicate = bW -> bW.bodyAsString().contains(description);
    val step =
        new VerificationStep.StepBuilder<OCSPBodyWrapper>(
            ErpAfos.A_24467.getRequirement(),
            "Der OcspResponse im String Body enthält nicht den Inhalt: " + description);
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxAcceptBundle> isQesValid() {
    Predicate<ErxAcceptBundle> hasOcspTokens =
        bundle -> !LocalVerifier.parse(bundle.getSignedKbvBundle()).getOcspTokens().isEmpty();
    Predicate<ErxAcceptBundle> verifyOcspTokens =
        bundle -> LocalVerifier.parse(bundle.getSignedKbvBundle()).verify();
    val step =
        new VerificationStep.StepBuilder<ErxAcceptBundle>(
            ErpAfos.A_20159.getRequirement(), "Die QES enthält keine oder ungültige OcspResponses");
    return step.predicate(hasOcspTokens).predicate(verifyOcspTokens).accept();
  }

  public static VerificationStep<ErxAcceptBundle> replacedOcspResp(OCSPToken originOcspToken) {
    Predicate<ErxAcceptBundle> predicate =
        bundle -> {
          val ocspTokens = LocalVerifier.parse(bundle.getSignedKbvBundle()).getOcspTokens();
          return ocspTokens.stream()
              .noneMatch(it -> Arrays.equals(it.getEncoded(), originOcspToken.getEncoded()));
        };
    val step =
        new VerificationStep.StepBuilder<ErxAcceptBundle>(
            ErpAfos.A_20159.getRequirement(), "Die ursprüngliche OcspResponse wurde nicht ersetzt");
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxAcceptBundle> containsOcspResp(OCSPToken originOcspToken) {
    Predicate<ErxAcceptBundle> predicate =
        bundle -> {
          val ocspTokens = LocalVerifier.parse(bundle.getSignedKbvBundle()).getOcspTokens();
          return ocspTokens.stream()
              .anyMatch(it -> Arrays.equals(it.getEncoded(), originOcspToken.getEncoded()));
        };
    val step =
        new VerificationStep.StepBuilder<ErxAcceptBundle>(
            ErpAfos.A_20159.getRequirement(),
            "Die ursprüngliche OcspResponse ist in der QES enthalten");
    return step.predicate(predicate).accept();
  }
}
