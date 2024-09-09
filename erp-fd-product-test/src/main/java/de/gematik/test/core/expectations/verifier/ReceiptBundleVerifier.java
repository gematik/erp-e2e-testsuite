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

package de.gematik.test.core.expectations.verifier;

import static java.lang.String.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.IbmAgreements;
import de.gematik.test.erezept.fhir.resources.erp.ErxReceipt;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.cms.CMSSignedData;

public class ReceiptBundleVerifier {

  private ReceiptBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxReceipt> entryFullUrlIsUuid() {
    Predicate<ErxReceipt> predicate =
        receipt ->
            receipt.getEntry().stream()
                .filter(entry -> !entry.getFullUrl().startsWith("urn:uuid"))
                .findAny()
                .isEmpty();
    val step =
        new VerificationStep.StepBuilder<ErxReceipt>(
            IbmAgreements.RECEIPT_ENTRY_FULLURL,
            "As an agreement with IBM the entries in receipts has to have a fullURL as urn:uuid:");
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxReceipt> compAuthorRefIsUuid() {
    Predicate<ErxReceipt> author =
        receipt -> receipt.getAuthor().getReference().startsWith("urn:uuid");
    val step =
        new VerificationStep.StepBuilder<ErxReceipt>(
            IbmAgreements.RECEIPT_REFERENCE_IS_UUID,
            "As an agreement with IBM the reference bundle.composition.author. in receipts has to"
                + " be a urn:uuid:");
    return step.predicate(author).accept();
  }

  public static VerificationStep<ErxReceipt> signatureRefIsUuid() {
    Predicate<ErxReceipt> signature =
        receipt -> receipt.getSignature().getWho().getReference().startsWith("urn:uuid:");
    val step =
        new VerificationStep.StepBuilder<ErxReceipt>(
            IbmAgreements.RECEIPT_REFERENCE_IS_UUID,
            "As an agreement with IBM the Reference bundle.signature in receipts has to be a"
                + " urn:uuid:");
    return step.predicate(signature).accept();
  }

  public static VerificationStep<ErxReceipt> compSectionRefIsUuid() {
    Predicate<ErxReceipt> compSection =
        receipt ->
            receipt
                .getQesDigestRefInComposSect()
                .getEntryFirstRep()
                .getReference()
                .startsWith("urn:uuid:");
    val step =
        new VerificationStep.StepBuilder<ErxReceipt>(
            IbmAgreements.RECEIPT_REFERENCE_IS_UUID,
            "As an agreement with IBM the Reference bundle.composition.signature in receipts has to"
                + " be a urn:uuid:");
    return step.predicate(compSection).accept();
  }

  public static VerificationStep<ErxReceipt> compareSignatureHashWith(
      byte[] docSignedDocument, ErpAfos erpAfo) {
    Predicate<ErxReceipt> compareHash =
        receipt ->
            Base64.getEncoder()
                .encodeToString(receipt.getQesDigestBinary().getContent())
                .equals(
                    getHashAsBase64String(Base64.getEncoder().encodeToString(docSignedDocument)));
    val step =
        new VerificationStep.StepBuilder<ErxReceipt>(
            erpAfo.getRequirement(), format(erpAfo.getDescription(), compareHash));
    return step.predicate(compareHash).accept();
  }

  @SneakyThrows
  @Nullable
  public static String getHashAsBase64String(String signedDocument) {
    val signedData =
        new CMSSignedData(
            Base64.getDecoder().decode(signedDocument.getBytes(StandardCharsets.UTF_8)));
    val docHash =
        signedData.getSignerInfos().getSigners().stream()
            .toList()
            .get(0)
            .getSignedAttributes()
            .get(CMSAttributes.messageDigest)
            .getEncoded();
    byte[] sliced = Arrays.copyOfRange(docHash, docHash.length - 32, docHash.length);
    return Base64.getEncoder().encodeToString(sliced);
  }
}
