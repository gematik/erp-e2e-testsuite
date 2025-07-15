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

package de.gematik.test.erezept.app.cfg;

import kong.unirest.core.Unirest;
import kong.unirest.core.json.JSONObject;
import lombok.val;

public class ScenarioFinalizer {
  public ScenarioFinalizer(String baseUrl) {
    Unirest.config().defaultBaseUrl(baseUrl);
  }

  public boolean sendTestInfo(String sessionId, String scenarioName, String scenarioStatus) {
    val jsonBody =
        new JSONObject()
            .put("sessionId", sessionId)
            .put("testName", scenarioName)
            .put("testStatus", scenarioStatus);

    val response =
        Unirest.post("setTestInfo")
            .header("Content-Type", "application/json")
            .body(jsonBody)
            .asJson();

    return response.getStatus() == 200;
  }
}
