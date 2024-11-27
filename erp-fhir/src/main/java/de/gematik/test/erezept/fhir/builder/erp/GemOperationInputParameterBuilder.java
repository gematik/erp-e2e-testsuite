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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.resources.erp.GemCloseOperationParameters;
import de.gematik.test.erezept.fhir.resources.erp.GemDispenseOperationParameters;

/**
 * Not a real builder, but a factory for the real builders. This class is used to instantiate the
 * real builders for the different types of operations.
 */
public class GemOperationInputParameterBuilder {

  private GemOperationInputParameterBuilder() {
    throw new IllegalArgumentException("utility class");
  }

  public static GemDispenseCloseOperationPharmaceuticalsBuilder<GemDispenseOperationParameters>
      forDispensingPharmaceuticals() {
    return new GemDispenseCloseOperationPharmaceuticalsBuilder<>(
        ErpWorkflowStructDef.DISPENSE_OPERATION_INPUT_PARAM,
        GemDispenseOperationParameters::new,
        true);
  }

  public static GemDispenseCloseOperationPharmaceuticalsBuilder<GemCloseOperationParameters>
      forClosingPharmaceuticals() {
    return new GemDispenseCloseOperationPharmaceuticalsBuilder<>(
        ErpWorkflowStructDef.CLOSE_OPERATION_INPUT_PARAM, GemCloseOperationParameters::new, false);
  }

  public static GemCloseOperationDiGABuilder<GemCloseOperationParameters> forClosingDiGA() {
    return new GemCloseOperationDiGABuilder<>(
        ErpWorkflowStructDef.CLOSE_OPERATION_INPUT_PARAM, GemCloseOperationParameters::new);
  }
}
