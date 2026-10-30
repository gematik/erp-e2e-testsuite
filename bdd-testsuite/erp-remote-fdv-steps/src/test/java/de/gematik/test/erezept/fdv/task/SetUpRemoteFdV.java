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

package de.gematik.test.erezept.fdv.task;

import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.fdv.cfg.RemoteFdVBaseConfigWrapper;
import de.gematik.test.erezept.remotefdv.client.DeviceFinder;
import de.gematik.test.erezept.remotefdv.client.DeviceRequests;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class SetUpRemoteFdV implements Task {
  private final String userName;
  private final RemoteFdVBaseConfigWrapper configWrapper;

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val useTheRemoteFdv = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);

    val patient = configWrapper.getFdVUserConfig(userName);
    val remoteFdvConfig = configWrapper.getFdVConfig(patient.getRemoteFdV());
    val deviceManagement = remoteFdvConfig.getDeviceManagement();

    if (deviceManagement.isActive()) {
      val deviceId = deviceManagement.deviceId();
      if (deviceId != null) {
        // use a specific device id provided in the config
        useTheRemoteFdv.sendRequest(DeviceRequests.claimDevice(deviceId));
        log.info("Claimed device with id {}", deviceId);
      } else {
        // use any (free) device
        val foundDeviceId = DeviceFinder.findDevice(useTheRemoteFdv.client);
        useTheRemoteFdv.sendRequest(DeviceRequests.claimDevice(foundDeviceId));
        log.info("Found a device with id {} and claimed it", foundDeviceId);
      }
    }
    useTheRemoteFdv.sendRequest(PatientRequests.startFdV()).getExpectedResourcesList();
    useTheRemoteFdv
        .sendRequest(PatientRequests.loginWithKvnr(patient.getKvnr()))
        .getExpectedResourcesList();
  }

  public static SetUpRemoteFdV forUser(String user, RemoteFdVBaseConfigWrapper config) {
    return new SetUpRemoteFdV(user, config);
  }
}
