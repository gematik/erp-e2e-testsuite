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

package de.gematik.test.erezept.config.dto.app;

import de.gematik.test.erezept.config.dto.BaseConfigurationDto;
import java.util.List;
import lombok.Data;

@Data
public class ErpAppConfigurationBase implements BaseConfigurationDto {

  private boolean shouldLogCapabilityStatement = false;
  private List<AppConfiguration> apps;
  private List<ErpActorConfiguration> users;
  private List<DeviceConfiguration> devices;
  private List<AppiumConfiguration> appium;
}