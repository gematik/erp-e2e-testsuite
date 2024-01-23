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

package de.gematik.test.konnektor.commands;

import static java.text.MessageFormat.format;

import de.gematik.test.konnektor.commands.options.VerifyDocumentOptions;
import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.ObjectFactory;
import de.gematik.ws.conn.signatureservice.v7.VerificationResultType;
import javax.xml.ws.Holder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import oasis.names.tc.dss._1_0.core.schema.SignatureObject;
import oasis.names.tc.dss_x._1_0.profiles.verificationreport.schema_.ReturnVerificationReport;

@Slf4j
public class VerifyDocumentCommand extends AbstractKonnektorCommand<Boolean> {

  private final byte[] content;
  private final VerifyDocumentOptions options;

  public VerifyDocumentCommand(byte[] content) {
    this(content, VerifyDocumentOptions.getDefaultOptions());
  }

  public VerifyDocumentCommand(byte[] content, VerifyDocumentOptions options) {
    this.content = content;
    this.options = options;
  }

  @Override
  public Boolean execute(ContextType ctx, ServicePortProvider serviceProvider) {
    ctx.setUserId(null);
    val factory = new ObjectFactory();

    val servicePort = serviceProvider.getSignatureService();

    val optionalInputs = factory.createVerifyDocumentOptionalInputs();
    optionalInputs.setReturnVerificationReport(createVerificationReportOptions());

    val signatureObject = createSignatureObject();
    val includeRevocationInfo = options.isIncludeRevocationInfo();
    val outStatus = new Holder<Status>();
    val outResult = new Holder<VerificationResultType>();
    val outOptionals =
        new Holder<de.gematik.ws.conn.signatureservice.v7.VerifyDocumentResponse.OptionalOutputs>();

    log.trace(
        format(
            "Verify Document of length {0} Bytes with SignatureType {1}",
            this.content.length, this.options.getSignatureType()));

    this.executeAction(
        () ->
            servicePort.verifyDocument(
                ctx,
                options.getTvMode(),
                optionalInputs,
                null, // don't send a document, only the signature object!
                signatureObject,
                includeRevocationInfo,
                outStatus,
                outResult,
                outOptionals));

    return mapVerificationResult(outStatus, outResult);
  }

  private boolean mapVerificationResult(
      Holder<Status> status, Holder<VerificationResultType> result) {
    log.trace(
        format(
            "Verify Document result: result={0} / status={1}",
            result.value.getHighLevelResult(), status.value.getResult()));
    return !hasError(status) && isValid(result);
  }

  private boolean hasError(Holder<Status> status) {
    return status.value.getError() != null;
  }

  private boolean isValid(Holder<VerificationResultType> result) {
    return result.value.getHighLevelResult().equalsIgnoreCase("valid");
  }

  private ReturnVerificationReport createVerificationReportOptions() {
    val oasisProfilesFactory =
        new oasis.names.tc.dss_x._1_0.profiles.verificationreport.schema_.ObjectFactory();
    val returnVerificationReport = oasisProfilesFactory.createReturnVerificationReport();
    returnVerificationReport.setIncludeVerifier(options.isIncludeVerifier());
    returnVerificationReport.setIncludeCertificateValues(options.isIncludeCertificateValue());
    returnVerificationReport.setIncludeRevocationValues(options.isIncludeRevocationValue());
    returnVerificationReport.setExpandBinaryValues(options.isExpandBinaryValues());
    return returnVerificationReport;
  }

  private SignatureObject createSignatureObject() {
    val oasisCoreFactory = new oasis.names.tc.dss._1_0.core.schema.ObjectFactory();
    val base64Signature = oasisCoreFactory.createBase64Signature();

    base64Signature.setValue(content);
    base64Signature.setType(options.getSignatureType().getUrn());

    val signatureObject = new SignatureObject();
    signatureObject.setBase64Signature(base64Signature);
    return signatureObject;
  }
}
