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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.valuesets.DocumentType;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import de.gematik.test.erezept.screenplay.util.DispenseReceipt;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class RetrieveReceiptAgain implements Task {

  private final DequeStrategyEnum deque;

  public RetrieveReceiptAgain(DequeStrategyEnum deque) {
    this.deque = deque;
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val pharmacyStack = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val receiptToRetrieve = deque.chooseFrom(pharmacyStack.getDispensedPrescriptions());

    val prescriptionId = receiptToRetrieve.getReceipt().getPrescriptionId();
    val taskId = receiptToRetrieve.getTaskId();
    val secret = receiptToRetrieve.getSecret();

    val cmd = new TaskGetByIdCommand(taskId, secret);
    val response = erpClient.request(cmd);
    val prescriptionBundle = response.getResource(cmd.expectedResponseBody());

    val erxReceipt =
        prescriptionBundle
            .getReceipt()
            .orElseThrow(
                () ->
                    new AssertionError(
                        format(
                            "Retrieved ErxPrescriptionBundle for Task {0} does not contain an ErxReceipt",
                            taskId)));

    assertEquals(
        prescriptionId,
        erxReceipt.getPrescriptionId(),
        format(
            "Retrieved ErxPrescriptionBundle for Task {0} does contain an "
                + "ErxReceipt but PrescriptionId does not match the Expectation"));
    assertEquals(
        DocumentType.RECEIPT,
        erxReceipt.getDocumentType(),
        format(
            "Retrieved ErxPrescriptionBundle for Task {0} "
                + "is expected to have DocumentType {0} but has {1}",
            DocumentType.RECEIPT, erxReceipt.getDocumentType()));

    // after checking the bundle, put the receipt on the stack
    val subjectKvid = receiptToRetrieve.getSubjectKvid();
    pharmacyStack.appendDispensedPrescriptions(
        new DispenseReceipt(subjectKvid, taskId, prescriptionId, secret, subjectKvid, erxReceipt));
  }

  public static RetrieveReceiptAgain fromStack(String order) {
    return fromStack(DequeStrategyEnum.fromString(order));
  }

  public static RetrieveReceiptAgain fromStack(DequeStrategyEnum deque) {
    return new RetrieveReceiptAgain(deque);
  }
}