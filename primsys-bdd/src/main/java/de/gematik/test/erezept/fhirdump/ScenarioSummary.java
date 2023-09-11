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

package de.gematik.test.erezept.fhirdump;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"id", "mainActor", "name", "tags", "description"})
public class ScenarioSummary {

  private static final String MAIN_ACTOR_TAG = "@Hauptdarsteller";
  private static final String AFO_TAG = "@AFO-ID";
  private static final String USE_CASE_TAG = "@AF-ID";

  private String id;
  private String name;
  private String description;
  private String feature;
  private String featureFile;
  private List<String> tags = new ArrayList<>();

  public void setDescription(String description) {
    this.description = description.trim();
  }

  public Optional<String> getMainActor() {
    return this.tags.stream()
        .filter(tag -> tag.contains(MAIN_ACTOR_TAG))
        .map(tag -> tag.split(":")[1])
        .findFirst();
  }

  public List<String> getRequirements() {
    return this.getTagValues(AFO_TAG);
  }

  public List<String> getUseCases() {
    return this.getTagValues(USE_CASE_TAG);
  }

  private List<String> getTagValues(String filterTag) {
    return this.tags.stream()
        .filter(tag -> tag.contains(filterTag))
        .map(tag -> tag.split(":")[1])
        .toList();
  }
}
