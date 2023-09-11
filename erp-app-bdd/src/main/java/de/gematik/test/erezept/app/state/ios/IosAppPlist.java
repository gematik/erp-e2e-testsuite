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

package de.gematik.test.erezept.app.state.ios;

import com.dd.plist.BinaryPropertyListWriter;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

public class IosAppPlist {

  private static final List<String> TOOLTIP_KEYS = List.of("MainViewTooltipId_1000", "MainViewTooltipId_900", "MainViewTooltipId_9900");

  private final NSObject dict;

  private IosAppPlist(NSObject dict) {
    this.dict = dict;
  }
  
  public static PlistComposer forE2ETest() {
    return new PlistComposer().withoutTooltips().allowTracking(true).hideWelcomeDrawer();
  }
  

  public String toXml() {
    return this.dict.toXMLPropertyList();
  }
  
  @SneakyThrows
  public byte[] toBinary() {
    val baos = new ByteArrayOutputStream();
    BinaryPropertyListWriter.write(this.dict, baos);
    return baos.toByteArray();
  }

  public byte[] toBase64() {
    return Base64.getEncoder().encode(this.toBinary());
  }
  
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class PlistComposer {
    private final NSDictionary dict = new NSDictionary();

    public PlistComposer withoutTooltips() {
      val toolTipsMap = new HashMap<String, Boolean>();
      TOOLTIP_KEYS.forEach(key -> toolTipsMap.put(key, true));
      dict.put("TOOLTIPS", toolTipsMap);
      return this;
    }

    public PlistComposer allowTracking(boolean allow) {
      dict.put("kAppTrackingAllowed", allow);
      return this;
    }

    public PlistComposer hideWelcomeDrawer() {
      return hideWelcomeDrawer(true);
    }
    
    public PlistComposer hideWelcomeDrawer(boolean allow) {
      dict.put("kHideWelcomeDrawer", allow);
      return this;
    }
    
    public IosAppPlist asPlist() {
      return new IosAppPlist(this.dict);
    }
  }
}
