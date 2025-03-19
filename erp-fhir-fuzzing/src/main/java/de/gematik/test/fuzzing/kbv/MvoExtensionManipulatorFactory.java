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

package de.gematik.test.fuzzing.kbv;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionIdExtension;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;

public class MvoExtensionManipulatorFactory {

  private MvoExtensionManipulatorFactory() {
    throw new AssertionError("Do not instantiate");
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>
      getMvoExtensionKennzeichenFalsifier() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "MVO Kennzeichen = false mit Nummerierung und Zeitraum",
            b -> {
              val mvoKennzeichenExt = getMvoKennzeichenExtensionFrom(b);
              mvoKennzeichenExt
                  .getValue()
                  .castToBoolean(mvoKennzeichenExt.getValue())
                  .setValue(false);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO ohne Kennzeichen und mit Nummerierung und Zeitraum",
            b -> {
              val mvoExt = getMvoExtensionFrom(b);
              val flagExt = mvoExt.getExtensionByUrl("Kennzeichen");
              mvoExt.getExtension().remove(flagExt);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO Kennzeichen = true ohne Nummerierung und mit Zeitraum",
            b -> {
              val mvoExt = getMvoExtensionFrom(b);
              val numExt = mvoExt.getExtensionByUrl("Nummerierung");
              mvoExt.getExtension().remove(numExt);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO Kennzeichen = true mit Nummerierung und ohne Zeitraum",
            b -> {
              val mvoExt = getMvoExtensionFrom(b);
              val periodExt = mvoExt.getExtensionByUrl("Zeitraum");
              mvoExt.getExtension().remove(periodExt);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO mit Datumsformat YYYY-MM-DDThh:mm:ss+hh:mm im Startdatum",
            b -> {
              val periodExt = getMvoPeriodExtensionFrom(b);
              val period = periodExt.getValue().castToPeriod(periodExt.getValue());
              period.getStartElement().setPrecision(TemporalPrecisionEnum.SECOND);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO mit Datumsformat YYYY-MM-DDThh:mm:ss+hh:mm im Enddatum",
            b -> {
              val periodExt = getMvoPeriodExtensionFrom(b);
              val period = periodExt.getValue().castToPeriod(periodExt.getValue());
              period.getEndElement().setPrecision(TemporalPrecisionEnum.SECOND);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO mit Datumsformat YYYY-MM-DDThh:mm:ss+hh:mm im Start- und Enddatum",
            b -> {
              val periodExt = getMvoPeriodExtensionFrom(b);
              val period = periodExt.getValue().castToPeriod(periodExt.getValue());
              period.getStartElement().setPrecision(TemporalPrecisionEnum.SECOND);
              period.getEndElement().setPrecision(TemporalPrecisionEnum.SECOND);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO mit Datumsformat YYYY im Startdatum",
            b -> {
              val periodExt = getMvoPeriodExtensionFrom(b);
              val period = periodExt.getValue().castToPeriod(periodExt.getValue());
              period.getStartElement().setPrecision(TemporalPrecisionEnum.YEAR);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO mit Datumsformat YYYY im Enddatum",
            b -> {
              val periodExt = getMvoPeriodExtensionFrom(b);
              val period = periodExt.getValue().castToPeriod(periodExt.getValue());
              period.getEndElement().setPrecision(TemporalPrecisionEnum.YEAR);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO mit Datumsformat YYYY im Start- und Enddatum",
            b -> {
              val periodExt = getMvoPeriodExtensionFrom(b);
              val period = periodExt.getValue().castToPeriod(periodExt.getValue());
              period.getStartElement().setPrecision(TemporalPrecisionEnum.YEAR);
              period.getEndElement().setPrecision(TemporalPrecisionEnum.YEAR);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO Zeitraum mit ungültigem Typ",
            b -> {
              val periodExt = getMvoPeriodExtensionFrom(b);
              periodExt.setValue(
                  new StringType(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
            }));
    manipulators.add(
        NamedEnvelope.of(
            "MVO ohne ID",
            b -> {
              val mvoExt = getMvoExtensionFrom(b);
              val flagExt = mvoExt.getExtensionByUrl(MultiplePrescriptionIdExtension.URL);
              mvoExt.getExtension().remove(flagExt);
            }));
    return manipulators;
  }

  private static Extension getMvoExtensionFrom(KbvErpBundle bundle) {
    return bundle.getMedicationRequest().getExtension().stream()
        .filter(
            extension ->
                extension
                    .getUrl()
                    .contains(KbvItaErpStructDef.MULTIPLE_PRESCRIPTION.getCanonicalUrl()))
        .findAny()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvErpBundle.class, KbvItaErpStructDef.MULTIPLE_PRESCRIPTION));
  }

  private static Extension getMvoKennzeichenExtensionFrom(KbvErpBundle bundle) {
    val mvo = getMvoExtensionFrom(bundle);
    return mvo.getExtensionByUrl("Kennzeichen");
  }

  private static Extension getMvoPeriodExtensionFrom(KbvErpBundle bundle) {
    val mvo = getMvoExtensionFrom(bundle);
    return mvo.getExtensionByUrl("Zeitraum");
  }
}
