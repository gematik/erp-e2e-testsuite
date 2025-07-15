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

package de.gematik.test.erezept.app;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.cfg.AppiumDriverFactory;
import de.gematik.test.erezept.app.cfg.ErpAppConfiguration;
import de.gematik.test.erezept.config.ConfigurationReader;
import lombok.val;

public class Main {

  public static void main(String[] args) {

    val config = ConfigurationReader.forAppConfiguration().wrappedBy(ErpAppConfiguration::fromDto);
    for (var i = 0; i < 10; i++) {
      val start = System.currentTimeMillis();
      try {
        val driver = AppiumDriverFactory.forUser("Connection Test", "Alice", config);
        driver.tearDown();
      } catch (Exception e) {
        System.out.println(format("Error after {0}ms", System.currentTimeMillis() - start));
        System.out.println(e.getMessage() + "\n-----------------------");
      }
    }
  }
}
