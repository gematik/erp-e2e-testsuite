/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.config.dto.app;

import de.gematik.bbriccs.cfg.NamedConfigurationElement;
import lombok.Data;

@Data
public class AppiumConfiguration implements NamedConfigurationElement {

  private String name;
  private String url;
  private String accessKey;
  private String version;
  private String provisioningProfilePostfix;
  private int maxWaitTimeout = 5000; // duration until timeout and finally NoSuchElementException
  private int pollingInterval = 50; // polling interval in milliseconds
  private int maxRefreshTimeout =
      30000; // duration to wait for the prescription refresh to finish until TimeOutException
}
