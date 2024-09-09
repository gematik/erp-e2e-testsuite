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

package de.gematik.test.konnektor.commands;

import static java.text.MessageFormat.format;

import de.gematik.test.konnektor.commands.options.SigningCryptType;
import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.signatureservice.v7.SignRequest;
import de.gematik.ws.conn.signatureservice.v7.SignResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class SignDocumentsCommand extends AbstractKonnektorCommand<List<SignResponse>> {

  private final String tvMode = "NONE"; // NOSONAR
  private final SigningCryptType crypt = SigningCryptType.RSA; // NOSONAR
  private final String cardHandle;
  private final List<SignRequest> signRequests;

  public SignDocumentsCommand(String cardHandle, List<SignRequest> signRequests) {
    this.cardHandle = cardHandle;
    this.signRequests = signRequests;
  }

  @Override
  public List<SignResponse> execute(ContextType ctx, ServicePortProvider serviceProvider) {
    val servicePort = serviceProvider.getSignatureService();

    val jobNumber = this.executeSupplier(() -> servicePort.getJobNumber(ctx));
    log.trace(format("Sign Document with {0} for JobNumber {1}", crypt.getValue(), jobNumber));
    val response =
        this.executeSupplier(
            () ->
                servicePort.signDocument(
                    cardHandle, crypt.getValue(), ctx, tvMode, jobNumber, signRequests));

    log.trace(
        format(
            "Response {0} with {1} Elements",
            response.getClass().getSimpleName(), response.size()));
    return response;
  }
}
