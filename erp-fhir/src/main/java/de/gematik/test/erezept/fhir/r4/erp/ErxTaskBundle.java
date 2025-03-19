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

package de.gematik.test.erezept.fhir.r4.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class ErxTaskBundle extends ErxBundle {

  public List<ErxTask> getTasks() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .map(ErxTask::fromTask)
        .toList();
  }

  public ErxTask getLatestTask() {
    Comparator<? super ErxTask> comparator =
        Comparator.comparing(ErxTask::getAuthoredOn).reversed();
    return this.getTasks().stream().sorted(comparator).toList().get(0);
  }
}
