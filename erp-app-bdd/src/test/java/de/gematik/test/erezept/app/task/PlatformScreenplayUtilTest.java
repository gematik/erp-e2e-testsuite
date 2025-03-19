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

package de.gematik.test.erezept.app.task;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.task.android.SetUpAndroidDevice;
import de.gematik.test.erezept.app.task.ios.SetUpIosDevice;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import lombok.val;
import org.junit.jupiter.api.Test;

class PlatformScreenplayUtilTest {

  @Test
  void chooseTaskForPlatform() {
    val environment = new EnvironmentConfiguration();
    environment.setName("RU");

    val androidTask =
        PlatformScreenplayUtil.chooseTaskForPlatform(
            PlatformType.ANDROID,
            () -> new SetUpAndroidDevice(InsuranceTypeDe.GKV, null),
            () -> new SetUpIosDevice(environment, InsuranceTypeDe.BG, null));
    assertSame(SetUpAndroidDevice.class, androidTask.getClass());

    val iosTask =
        PlatformScreenplayUtil.chooseTaskForPlatform(
            PlatformType.IOS,
            () -> new SetUpAndroidDevice(InsuranceTypeDe.GKV, null),
            () -> new SetUpIosDevice(environment, InsuranceTypeDe.BG, null));
    assertSame(SetUpIosDevice.class, iosTask.getClass());

    assertThrows(
        UnsupportedPlatformException.class,
        () ->
            PlatformScreenplayUtil.chooseTaskForPlatform(
                PlatformType.DESKTOP,
                () -> new SetUpAndroidDevice(InsuranceTypeDe.GKV, null),
                () -> new SetUpIosDevice(environment, InsuranceTypeDe.BG, null)));
  }
}
