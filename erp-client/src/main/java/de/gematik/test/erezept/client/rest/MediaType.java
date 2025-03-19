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

package de.gematik.test.erezept.client.rest;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.erezept.fhir.exceptions.UnsupportedEncodingException;
import lombok.val;

/**
 * Diese Enumeration beschreibt die beiden gängigsten Media-Types (MIME) die von einem FHIR-Server
 * bzw. FD erwartet werden.
 */
public enum MediaType {
  ACCEPT_FHIR_XML("application/fhir+xml;q=1.0, application/xml+fhir;q=0.9"),
  FHIR_XML("application/fhir+xml"),
  ACCEPT_FHIR_JSON("application/fhir+json;q=1.0, application/json+fhir;q=0.9"),
  FHIR_JSON("application/fhir+json"),
  EMPTY(""),
  UNKNOWN("*/*");

  private String stringValue;

  MediaType(String stringValue) {
    this.stringValue = stringValue;
  }

  /**
   * Gibt die String-Repräsentation (MIME-Type) des enum wieder wie sie im Header eines
   * Request/Response verwendet werden kann.
   *
   * @return
   */
  public String asString() {
    return this.asString(false);
  }

  /**
   * * Liefert den Accept-Type entweder alleine oder mit angefügten Parametern. * gemäß
   * https://tools.ietf.org/html/rfc1945#section-3.6
   *
   * @param removeParameters entfernt mögliche Parameter hinter dem Accept-Type
   * @return der Accept-Type als String
   */
  public String asString(boolean removeParameters) {
    return this.asString(removeParameters, false);
  }

  public String asString(boolean removeParameters, boolean removeApplication) {
    String ret;
    if (removeParameters) {
      ret = this.stringValue.split(";")[0];
    } else {
      ret = this.stringValue;
    }

    if (removeApplication) {
      ret = ret.replace("application/", "");
    }
    return ret.toLowerCase();
  }

  public EncodingType toFhirEncoding() {
    return switch (this) {
      case FHIR_XML, ACCEPT_FHIR_XML -> EncodingType.XML;
      case FHIR_JSON, ACCEPT_FHIR_JSON -> EncodingType.JSON;
      default -> throw new UnsupportedEncodingException(
          format("MediaType {0} cannot be translated to proper FHIR encoding", this.asString()));
    };
  }

  /**
   * Erstelle eine enum anhand des Strings der im Response-Header enthalten ist
   *
   * @param stringValue ist der 'content-type' Wert aus dem Response-Header
   * @return ist der entsprechende ContentType als enum
   */
  public static MediaType fromString(String stringValue) {
    if (stringValue == null || stringValue.isEmpty()) {
      return MediaType.EMPTY;
    }

    val tokens = stringValue.split(";"); // https://tools.ietf.org/html/rfc1945#section-3.6
    val mts = tokens[0].toLowerCase(); // contains only media-type (type/sub-type) as lower case

    MediaType mediaType;
    if (mts.contains(MediaType.FHIR_XML.asString())) {
      mediaType = MediaType.FHIR_XML;
    } else if (mts.contains(MediaType.FHIR_JSON.asString())) {
      mediaType = MediaType.FHIR_JSON;
    } else {
      mediaType = UNKNOWN;
      mediaType.stringValue = stringValue;
    }

    return mediaType;
  }

  public boolean isEquivalentTo(MediaType other) {
    val myMts = stringValue.split(";")[0].toLowerCase();
    val otherMts = other.stringValue.split(";")[0].toLowerCase();

    return myMts.equals(otherMts);
  }

  @Override
  public String toString() {
    return this.stringValue;
  }
}
