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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

/**
 * <br>
 * <b>Profile:</b> de.gematik.erezept-workflow.r4 (1.0.3) <br>
 * <b>File:</b> ValueSet-DOCUMENTTYPE.json <br>
 * <br>
 * <b>Publisher:</b> gematik GmbH <br>
 * <b>Published:</b> None <br>
 * <b>Status:</b> draft
 */
@Getter
public enum DocumentType implements IValueSet {
  PRESCRIPTION("1", "Health Care Provider Prescription"),
  CONFIRMATION("2", "Patient Confirmation"),
  RECEIPT("3", "Receipt"),
  ;

  public static final ErpWorkflowCodeSystem CODE_SYSTEM = ErpWorkflowCodeSystem.DOCUMENT_TYPE;
  public static final String VERSION = "1.0.3";
  public static final String DESCRIPTION = "Type of document depending on the recipient.";
  public static final String PUBLISHER = "gematik GmbH";

  private final String code;
  private final String display;
  private final String definition;

  DocumentType(String code, String display) {
    this(code, display, "N/A definition in profile");
  }

  DocumentType(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  @Override
  public ErpWorkflowCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static DocumentType fromCode(@NonNull String coding) {
    return Arrays.stream(DocumentType.values())
        .filter(dt -> dt.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(DocumentType.class, coding));
  }
}
