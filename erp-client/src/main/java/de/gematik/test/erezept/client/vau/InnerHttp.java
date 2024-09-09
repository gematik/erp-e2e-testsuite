/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.client.vau;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/** Utility-class for wrapping and unwrapping the inner-HTTP */
@Slf4j
public class InnerHttp {

  private static final String LINE_BREAK = "\r\n";
  private static final String DOUBLE_LINE_BREAK = LINE_BREAK + LINE_BREAK;

  static final String HTTP_V = "HTTP/1.1";

  private InnerHttp() {
    throw new AssertionError();
  }

  /**
   * Encode the given command to a UTF-8 String which can be used as the inner-HTTP Request in VAU
   * Note: Originally taken from testng-testtool Request.genHttpRequestAsString
   *
   * @param method is HTTP Method of the inner HTTP Request
   * @param path is the path where the inner HTTP Request should go to
   * @param headers are the headers for the inner HTTP Request
   * @param body is the body of the inner HTTP Request
   * @return The command as a UTF-8 encoded HTTP-Request
   */
  public static String encode(
      HttpRequestMethod method, String path, Map<String, String> headers, String body) {
    StringBuilder ret = new StringBuilder();

    ret.append(method.name())
        .append(" ")
        .append(path)
        .append(" ")
        .append(HTTP_V)
        .append(LINE_BREAK);
    headers.forEach((key, value) -> ret.append(key).append(": ").append(value).append(LINE_BREAK));

    ret.append("content-length: ")
        .append(body.getBytes(StandardCharsets.UTF_8).length)
        .append(DOUBLE_LINE_BREAK);
    log.debug(format("Encode inner-HTTP for Request:\n{0}", ret));

    if (body.length() > 0) {
      ret.append(body);
      // wrap the body into <xmp> to be able to show XML-content correctly in HTML-reports
      log.debug(
          format(
              "Encode FHIR Resource for the Body of the inner-HTTP Request:\n<xmp>{0}</xmp>",
              body));
    } else {
      log.debug("Inner-HTTP Request has empty Body");
    }

    return ret.toString();
  }

  public static Response decode(byte[] rawResponse) {
    val data = new String(rawResponse, StandardCharsets.UTF_8);
    if (data.isBlank())
      throw new VauException(format("response is not parsable, Response: {0}", data));

    val raw = data.split(DOUBLE_LINE_BREAK);
    val rawHeader = raw[0].split(LINE_BREAK);

    if (!rawHeader[0].contains(HTTP_V))
      throw new VauException(format("http protocol is missing; StatusLine: {0}", rawHeader[0]));

    val statusLine = rawHeader[0].substring(rawHeader[0].indexOf(HTTP_V));
    val statusLineItems = statusLine.split(" ");

    if (statusLineItems.length < 2) throw new VauException("status line isn't complete");

    int statusCode;
    try {
      statusCode = Integer.parseInt(statusLineItems[1]);
    } catch (NumberFormatException e) {
      throw new VauException("status code should be a number");
    }

    return new Response(
        statusLineItems[0], statusCode, parseHeader(rawHeader), raw.length == 2 ? raw[1] : "");
  }

  private static Map<String, String> parseHeader(String[] rawHeader) {
    val ret = new HashMap<String, String>();
    Arrays.stream(rawHeader)
        .filter(x -> !x.startsWith(HTTP_V))
        .map(InnerHttp::parseHeaderLine)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(x -> ret.put(x.getKey(), x.getValue()));
    return ret;
  }

  private static Optional<SimpleEntry<String, String>> parseHeaderLine(String header) {
    val keyValue = header.split(": ?", 2);
    if (keyValue.length <= 1) {
      log.debug("header structure is invalid: " + header);
      return Optional.empty();
    }
    return Optional.of(new AbstractMap.SimpleEntry<>(keyValue[0], keyValue[1]));
  }
}
