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

package de.gematik.test.cardterminal.exceptions;

import static java.text.MessageFormat.format;

import kong.unirest.core.HttpResponse;

public class CardTerminalClientException extends RuntimeException {

  public CardTerminalClientException(String url, String reqBody, HttpResponse<String> resp) {
    super(
        format(
            "Card Terminal Client operation to {2} with {3} could not be performed. "
                + "StatusCode: {0} Body: {1}",
            resp.getStatus(), resp.getBody(), url, reqBody));
  }
}
