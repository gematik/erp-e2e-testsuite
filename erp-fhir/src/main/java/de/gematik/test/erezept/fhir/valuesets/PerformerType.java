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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <br>
 * <b>Profile:</b> de.gematik.erezept-workflow.r4 (1.0.3) <br>
 * <b>File:</b> ValueSet-PERFORMERTYPE.json <br>
 * <br>
 * <b>Publisher:</b> gematik GmbH <br>
 * <b>Published:</b> None <br>
 * <b>Status:</b> draft
 */
@Getter
@RequiredArgsConstructor
public enum PerformerType implements FromValueSet {
  PHARMACIST("urn:oid:1.2.276.0.76.4.32", "Apotheker"),
  PUBLIC_PHARMACY("urn:oid:1.2.276.0.76.4.54", "Öffentliche Apotheke"),
  ;

  public static final CommonCodeSystem CODE_SYSTEM = CommonCodeSystem.PERFORMER_TYPE;
  private final String code;
  private final String display;

  @Override
  public CommonCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static PerformerType fromCode(String coding) {
    return Arrays.stream(PerformerType.values())
        .filter(pt -> pt.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(PerformerType.class, coding));
  }

  public static PerformerType fromDisplay(String display) {
    return Arrays.stream(PerformerType.values())
        .filter(pt -> pt.display.contains(display))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(PerformerType.class, display));
  }
}
