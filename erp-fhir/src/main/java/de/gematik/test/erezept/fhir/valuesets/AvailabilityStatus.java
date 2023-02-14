/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;

/**
 * <br>
 * <b>Profile:</b> de.gematik.erezept-workflow.r4 (1.0.3) <br>
 * <b>File:</b> ValueSet-AVAILABILITYSTATUS.json <br>
 * <br>
 * <b>Publisher:</b> gematik GmbH <br>
 * <b>Published:</b> None <br>
 * <b>Status:</b> draft
 */
@Getter
public enum AvailabilityStatus implements IValueSet {
  AS_10("10", "sofort verfügbar"),
  AS_20("20", "noch Heute verfügbar"),
  AS_30("30", "am nächsten Werktag - vormittags"),
  AS_40("40", "am nächsten Werktag- nachmittags"),
  AS_50("50", "nicht verfügbar"),
  AS_90("90", "unbekannt"),
  ;

  public static final ErpWorkflowCodeSystem CODE_SYSTEM = ErpWorkflowCodeSystem.AVAILABILITY_STATUS;
  public static final String VERSION = "1.0.3";
  public static final String DESCRIPTION =
      "Type of the availability status for medication availability request";
  public static final String PUBLISHER = "gematik GmbH";

  private final String code;
  private final String display;
  private final String definition;

  AvailabilityStatus(String code, String display) {
    this(code, display, "N/A definition in profile");
  }

  AvailabilityStatus(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  @Override
  public ErpWorkflowCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Coding asCoding(ErpWorkflowCodeSystem codeSystem) {
    return asCoding(codeSystem, false);
  }

  public Coding asCoding(ErpWorkflowCodeSystem codeSystem, boolean withDisplay) {
    val coding = new Coding();
    coding.setCode(this.getCode());
    coding.setSystem(codeSystem.getCanonicalUrl());
    if (withDisplay) {
      coding.setDisplay(getDisplay());
    }
    return coding;
  }

  public static AvailabilityStatus fromCode(@NonNull String code) {
    return Arrays.stream(AvailabilityStatus.values())
        .filter(pt -> pt.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(AvailabilityStatus.class, code));
  }
}
