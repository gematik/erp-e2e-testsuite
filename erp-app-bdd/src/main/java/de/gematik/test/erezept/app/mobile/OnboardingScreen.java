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
package de.gematik.test.erezept.app.mobile;

import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;

public enum OnboardingScreen {
  START_SCREEN(0, 0),
  WELCOME_SCREEN(1, -1),
  TERMS_AND_PRIVACY_SCREEN(2, 1),
  SECURITY_SCREEN(3, 2),
  ANALYTICS_SCREEN(4, 3),
  FINISH_ALL(5, 4);

  private int iOSOrdinal;
  private int androidOrdinal;

  OnboardingScreen(int iOSOrdinal, int androidOrdinal) {
    this.iOSOrdinal = iOSOrdinal;
    this.androidOrdinal = androidOrdinal;
  }

  public int getiOSOrdinal() {
    if (iOSOrdinal < 0) {
      throw new FeatureNotImplementedException("This Onboarding Screen does not exist for iOS");
    }
    return iOSOrdinal;
  }

  public int getAndroidOrdinal() {
    if (androidOrdinal < 0) {
      throw new FeatureNotImplementedException("This Onboarding Screen does not exist for Android");
    }
    return androidOrdinal;
  }
}
