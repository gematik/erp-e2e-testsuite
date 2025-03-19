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
 */

package de.gematik.test.erezept.fhir.builder.kbv;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItvEvdgaStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItvEvdgaVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvHealthAppRequest;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.DeviceRequest.DeviceRequestStatus;
import org.hl7.fhir.r4.model.DeviceRequest.RequestIntent;
import org.hl7.fhir.r4.model.Reference;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class KbvHealthAppRequestBuilder
    extends ResourceBuilder<KbvHealthAppRequest, KbvHealthAppRequestBuilder> {

  private KbvItvEvdgaVersion version = KbvItvEvdgaVersion.getDefaultVersion();

  private PZN pzn;
  private String name;
  private Date authoredOn = new Date();
  private AccidentExtension accident;
  private Reference subjectReference;
  private Reference requesterReference;
  private Reference insuranceReference;
  private boolean relatesToSocialCompensationLaw = false;
  private DeviceRequestStatus status = DeviceRequestStatus.ACTIVE;
  private RequestIntent intent = RequestIntent.ORDER;

  public static KbvHealthAppRequestBuilder forPatient(KbvPatient patient) {
    val har = new KbvHealthAppRequestBuilder();
    har.subjectReference = patient.asReference();
    return har;
  }

  public KbvHealthAppRequestBuilder version(KbvItvEvdgaVersion version) {
    this.version = version;
    return this;
  }

  public KbvHealthAppRequestBuilder healthApp(String pzn, String name) {
    return healthApp(PZN.from(pzn), name);
  }

  public KbvHealthAppRequestBuilder healthApp(PZN pzn, String name) {
    this.pzn = pzn;
    this.name = name;
    return this;
  }

  public KbvHealthAppRequestBuilder requester(KbvPractitioner practitioner) {
    this.requesterReference = practitioner.asReference();
    return this;
  }

  public KbvHealthAppRequestBuilder insurance(KbvCoverage coverage) {
    this.insuranceReference = coverage.asReference();
    return this;
  }

  public KbvHealthAppRequestBuilder authoredOn(Date date) {
    this.authoredOn = date;
    return this;
  }

  public KbvHealthAppRequestBuilder accident(AccidentExtension accident) {
    this.accident = accident;
    return this;
  }

  public KbvHealthAppRequestBuilder relatesToSocialCompensationLaw(
      boolean relatesToSocialCompensationLaw) {
    this.relatesToSocialCompensationLaw = relatesToSocialCompensationLaw;
    return this;
  }

  public KbvHealthAppRequestBuilder status(DeviceRequest.DeviceRequestStatus status) {
    this.status = status;
    return this;
  }

  public KbvHealthAppRequestBuilder intent(DeviceRequest.RequestIntent intent) {
    this.intent = intent;
    return this;
  }

  @Override
  public KbvHealthAppRequest build() {
    val devReq =
        this.createResource(
            KbvHealthAppRequest::new, KbvItvEvdgaStructDef.HEALTH_APP_REQUEST, version);

    devReq.addExtension(
        KbvItvEvdgaStructDef.SER_EXTENSION.asBooleanExtension(relatesToSocialCompensationLaw));
    devReq.setStatus(status);
    devReq.setIntent(intent);

    Optional.ofNullable(accident).ifPresent(a -> devReq.addExtension(a.asExtension()));

    devReq.getCodeCodeableConcept().addCoding(pzn.asCoding()).setText(name);

    devReq.setAuthoredOnElement(new DateTimeType(authoredOn, TemporalPrecisionEnum.DAY));
    devReq.setSubject(subjectReference);
    devReq.setRequester(requesterReference);
    devReq.setInsurance(List.of(insuranceReference));

    return devReq;
  }
}
