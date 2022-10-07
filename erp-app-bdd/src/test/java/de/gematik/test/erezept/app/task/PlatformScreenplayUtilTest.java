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

package de.gematik.test.erezept.app.task;

import static org.junit.Assert.*;

import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.questions.android.HasReceivedPrescriptionOnAndroid;
import de.gematik.test.erezept.app.questions.ios.HasReceivedPrescriptionOnIos;
import de.gematik.test.erezept.app.task.android.SetUpAndroidDevice;
import de.gematik.test.erezept.app.task.ios.SetUpIosDevice;
import lombok.val;
import org.junit.Test;

public class PlatformScreenplayUtilTest {

  @Test
  public void chooseTaskForPlatform() {
    val androidTask =
        PlatformScreenplayUtil.chooseTaskForPlatform(
            PlatformType.ANDROID, SetUpAndroidDevice::new, SetUpIosDevice::new);
    assertSame(androidTask.getClass(), SetUpAndroidDevice.class);

    val iosTask =
        PlatformScreenplayUtil.chooseTaskForPlatform(
            PlatformType.IOS, SetUpAndroidDevice::new, SetUpIosDevice::new);
    assertSame(iosTask.getClass(), SetUpIosDevice.class);

    assertThrows(
        UnsupportedPlatformException.class,
        () ->
            PlatformScreenplayUtil.chooseTaskForPlatform(
                PlatformType.DESKTOP, SetUpAndroidDevice::new, SetUpIosDevice::new));
  }

  @Test
  public void chooseQuestionForPlatform() {
    val androidQuestion =
        PlatformScreenplayUtil.chooseQuestionForPlatform(
            PlatformType.ANDROID,
            HasReceivedPrescriptionOnAndroid::new,
            HasReceivedPrescriptionOnIos::new);
    assertSame(androidQuestion.getClass(), HasReceivedPrescriptionOnAndroid.class);

    val iosQuestion =
        PlatformScreenplayUtil.chooseQuestionForPlatform(
            PlatformType.IOS,
            HasReceivedPrescriptionOnAndroid::new,
            HasReceivedPrescriptionOnIos::new);
    assertSame(iosQuestion.getClass(), HasReceivedPrescriptionOnIos.class);

    assertThrows(
        UnsupportedPlatformException.class,
        () ->
            PlatformScreenplayUtil.chooseQuestionForPlatform(
                PlatformType.DESKTOP,
                HasReceivedPrescriptionOnAndroid::new,
                HasReceivedPrescriptionOnIos::new));
  }
}
