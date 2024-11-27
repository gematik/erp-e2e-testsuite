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

package de.gematik.test.erezept.abilities;

import com.beust.jcommander.Strings;
import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.test.erezept.client.exceptions.FhirValidationException;
import de.gematik.test.erezept.eml.*;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpCancelPrescription;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Ability;
import one.util.streamex.EntryStream;

@Slf4j
public class UseTheEpaMockClient implements Ability {

  private final EpaMockClient epaMockClient;
  @Delegate private final FhirCodec epaFhirCodec;

  private UseTheEpaMockClient(EpaMockClient epaMockClient, FhirCodec epaFhirCodec) {
    this.epaMockClient = epaMockClient;
    this.epaFhirCodec = epaFhirCodec;
  }

  public static UseTheEpaMockClient with(EpaMockClient epaMockClient) {
    return new UseTheEpaMockClient(epaMockClient, EpaFhirFactory.create());
  }

  public static UseTheEpaMockClient with(HttpBClient restClient) {
    return with(EpaMockClient.withRestClient(restClient));
  }

  public List<EpaOpProvidePrescription> downloadProvidePrescriptionBy(
      PrescriptionId prescriptionId) {

    val req = new DownloadRequestByPrescriptionId(prescriptionId.getValue());
    val startDate = System.currentTimeMillis();
    val request = this.epaMockClient.pollRequest(req);

    val diff = System.currentTimeMillis() - startDate;
    log.info("Duration of call to EpaMockClient in millis: {}", diff);

    val res = extractAndValidateRequests(request, "provide-prescription-erp");
    return res.stream()
        .map(content -> epaFhirCodec.decode(EpaOpProvidePrescription.class, content))
        .toList();
  }

  public List<EpaOpCancelPrescription> downloadCancelPrescriptionBy(PrescriptionId prescriptionId) {
    val req = new DownloadRequestByPrescriptionId(prescriptionId.getValue());
    val request = this.epaMockClient.pollRequest(req);

    val res = extractAndValidateRequests(request, "cancel-prescription-erp");
    return res.stream()
        .map(content -> epaFhirCodec.decode(EpaOpCancelPrescription.class, content))
        .toList();
  }

  public List<EpaOpProvideDispensation> downloadProvideDispensationBy(
      PrescriptionId prescriptionId) {
    val req = new DownloadRequestByPrescriptionId(prescriptionId.getValue());
    val startTime = System.currentTimeMillis();

    // important when Prescription has arrived but Dispensation is on his way
    var count = 5;
    var request = this.epaMockClient.pollRequest(req);
    var dispensations = extractAndValidateRequests(request, "provide-dispensation-erp");

    while (dispensations.isEmpty() && count-- > 0) {
      request = this.epaMockClient.pollRequest(req);
      dispensations = extractAndValidateRequests(request, "provide-dispensation-erp");
    }

    val diff = System.currentTimeMillis() - startTime;
    log.info("Duration of call to EpaMockClient in millis: {}", diff);

    return dispensations.stream()
        .map(content -> epaFhirCodec.decode(EpaOpProvideDispensation.class, content))
        .toList();
  }

  private List<String> extractAndValidateRequests(List<ErpEmlLog> request, String filter) {
    val re =
        request.stream()
            .filter(log -> log.request().urlPath().contains(filter))
            .map(log -> log.request().bodyAsString())
            .toList();
    val validationResults =
        re.stream().map(epaFhirCodec::validate).filter(vr -> !vr.isSuccessful()).toList();
    if (!validationResults.isEmpty()) {
      val errorMessage =
          EntryStream.of(validationResults)
              .map(
                  entry ->
                      "Message: "
                          + entry.getKey()
                          + "\n "
                          + Strings.join("\n", entry.getValue().getMessages().toArray()))
              .joining("\n");
      throw new FhirValidationException(errorMessage);
    }
    return re;
  }

  public boolean setProvidePrescriptionApply(KVNR kvnr) {
    val identifier = RuleIdentifier.PROVIDE_PRESCRIPTION;
    val rule = RuleDto.reply(kvnr.getValue(), RuleTemplate.PROVIDE_PRESCRIPTION_SUCCESS);
    val putConfiguration = new PutConfiguration(identifier, rule);
    return epaMockClient.configRequest(putConfiguration);
  }

  public boolean setProvideDispensationApply(KVNR kvnr) {
    val identifier = RuleIdentifier.PROVIDE_DISPENSATION;
    val rule = RuleDto.reply(kvnr.getValue(), RuleTemplate.PROVIDE_DISPENSATION_SUCCESS);
    val putConfiguration = new PutConfiguration(identifier, rule);
    return epaMockClient.configRequest(putConfiguration);
  }
}
