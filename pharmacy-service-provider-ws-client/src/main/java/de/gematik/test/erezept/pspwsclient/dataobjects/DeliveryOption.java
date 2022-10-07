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

package de.gematik.test.erezept.pspwsclient.dataobjects;

public enum DeliveryOption {
  SHIPMENT,
  DELIVERY,
  ON_PREMISE;

  public static DeliveryOption defineDeliveryOption(String deliveryOption) {
    DeliveryOption result;
    switch (deliveryOption.toLowerCase()) {
      case "shipment":
      case "versandapotheke":
      case "versand":
      case "belieferung":
        result = SHIPMENT;
        break;
      case "abholen":
      case "pick_up":
      case "abholung":
      case "reservierung":
        result = ON_PREMISE;
        break;
      case "bote":
      case "lokalebelieferung":
      case "botendienst":
        result = DELIVERY;
        break;
      default:
        throw new InvalidDeliveryOptionException();
    }
    return result;
  }
}
