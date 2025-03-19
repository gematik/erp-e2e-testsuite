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

package de.gematik.test.erezept.toggle;

import de.gematik.bbriccs.toggle.FeatureToggle;
import java.util.function.Function;

public class FuzzingIncrementsToggle implements FeatureToggle<Double> {
  @Override
  public String getKey() {
    return "erp.prodtest.fuzzing.increments";
  }

  @Override
  public Function<String, Double> getConverter() {
    return Double::parseDouble;
  }

  @Override
  public Double getDefaultValue() {
    return 0.01;
  }
}
