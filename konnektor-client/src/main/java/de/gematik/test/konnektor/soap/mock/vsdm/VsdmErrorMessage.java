/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.konnektor.soap.mock.vsdm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VsdmErrorMessage {
  PROOF_OF_PRESENCE_ERROR_SIG(
      "Anwesenheitsnachweis konnte nicht erfolgreich durchgeführt werden (Fehler bei Prüfung der"
          + " HMAC-Sicherung)."),
  PROOF_OF_PRESENCE_WITHOUT_CHECKSUM(
      "Anwesenheitsnachweis konnte nicht erfolgreich durchgeführt werden (Prüfziffer fehlt im VSDM"
          + " Prüfungsnachweis)."),
  INVALID_PNW("Missing or invalid PNW query parameter"),
  INVALID_CHECKSUM_SIZE("Invalid size of Prüfziffer"),
  PROOF_OF_PRESENCE_INVALID_TIMESTAMP(
      "Anwesenheitsnachweis konnte nicht erfolgreich durchgeführt werden (Zeitliche Gültigkeit des"
          + " Anwesenheitsnachweis überschritten)."),
  FAILED_PARSING_PNW("Failed parsing PNW XML.");
  private final String text;
}
