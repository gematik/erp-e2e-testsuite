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

import de.gematik.bbriccs.fhir.builder.FakerBrick;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvHealthAppRequest;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.valuesets.AccidentCauseType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.val;

public class KbvHealthAppRequestFaker {

  private static final String ACCIDENT_KEY = "accident";

  private final KbvPatient patientSubject;
  private final Map<String, Consumer<KbvHealthAppRequestBuilder>> builderConsumers =
      new HashMap<>();

  private KbvHealthAppRequestFaker(KbvPatient patientSubject) {
    this.patientSubject = patientSubject;

    val fakerBrick = FakerBrick.getGerman();
    builderConsumers.put("diga_pzn", b -> b.healthApp(PZN.random(), fakerBrick.app().name()));
    builderConsumers.put(
        "ser_flag", b -> b.relatesToSocialCompensationLaw(fakerBrick.random().nextBoolean()));

    this.withRequester(KbvPractitionerFaker.builder().fake())
        .withInsurance(KbvCoverageFaker.builder().fake());

    // randomly set an accident extension
    if (fakerBrick.random().nextBoolean()) {
      val causeType = fakerBrick.randomEnum(AccidentCauseType.class);
      val accident =
          switch (causeType) {
            case OCCUPATIONAL_DISEASE:
              yield AccidentExtension.occupationalDisease();
            case ACCIDENT_AT_WORK:
              yield AccidentExtension.accidentAtWork(fakerBrick.date().past(10, TimeUnit.DAYS))
                  .atWorkplace();
            default:
              yield AccidentExtension.accident();
          };
      this.withAccident(accident);
    }
  }

  public static KbvHealthAppRequestFaker forPatient(KbvPatient patient) {
    return new KbvHealthAppRequestFaker(patient);
  }

  public static KbvHealthAppRequestFaker forRandomPatient() {
    return forPatient(KbvPatientFaker.builder().fake());
  }

  public KbvHealthAppRequestFaker withRequester(KbvPractitioner practitioner) {
    builderConsumers.put("requester", b -> b.requester(practitioner));
    return this;
  }

  public KbvHealthAppRequestFaker withInsurance(KbvCoverage coverage) {
    builderConsumers.put("insurance", b -> b.insurance(coverage));
    return this;
  }

  public KbvHealthAppRequestFaker withoutAccident() {
    builderConsumers.remove(ACCIDENT_KEY);
    return this;
  }

  public KbvHealthAppRequestFaker withAccident(AccidentExtension accident) {
    builderConsumers.put(ACCIDENT_KEY, b -> b.accident(accident));
    return this;
  }

  public KbvHealthAppRequest fake() {
    return this.toBuilder().build();
  }

  public KbvHealthAppRequestBuilder toBuilder() {
    val builder = KbvHealthAppRequestBuilder.forPatient(patientSubject);
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
