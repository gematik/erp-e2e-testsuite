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

package de.gematik.test.erezept.primsys.model;

import static de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder.createInternalErrorException;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import jakarta.ws.rs.core.Response;
import lombok.val;

public class FetchReceiptUseCase {

  private final Pharmacy pharmacy;

  public FetchReceiptUseCase(Pharmacy pharmacy) {
    this.pharmacy = pharmacy;
  }

  public Response fetchReceipt(String taskId, String secret) {
    return fetchReceipt(TaskId.from(taskId), Secret.from(secret));
  }

  public Response fetchReceipt(TaskId taskId, Secret secret) {
    val cmd = new TaskGetByIdCommand(taskId, secret);
    val prescriptionBundle = pharmacy.erpRequest2(cmd);
    val taskStatus = prescriptionBundle.getTask().getStatus();
    val receipt =
        prescriptionBundle
            .getReceipt()
            .orElseThrow(
                () ->
                    createInternalErrorException(
                        404,
                        format(
                            "Prescription {0} does not have a receipt (status: {1})",
                            taskId.getValue(), taskStatus)));
    val xml = pharmacy.encode(receipt, EncodingType.XML);
    return Response.ok(xml).build();
  }
}
