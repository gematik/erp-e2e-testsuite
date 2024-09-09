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

package de.gematik.test.erezept.client.rest;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.UnsupportedEncodingException;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class MediaTypeTest {

  // valid Media Types according to RFC 1945
  // https://tools.ietf.org/html/rfc1945#section-3.6
  private final List<String> jsonTypes =
      List.of(
          "application/fhir+json; fhirVersion=4.0; charset=utf-8",
          "application/fhir+json; fhirVersion=4.0",
          "application/fhir+json; charset=utf-8",
          "application/fhir+json;fhirVersion=4.0;charset=utf-8",
          "application/fhir+json;fhirVersion=4.0",
          "application/fhir+json;charset=utf-8",
          "application/fhir+json",
          "APPLICATION/FHIR+JSON");

  private final List<String> xmlTypes =
      List.of(
          "application/fhir+xml; fhirVersion=4.0; charset=utf-8",
          "application/fhir+xml; fhirVersion=4.0",
          "application/fhir+xml; charset=utf-8",
          "application/fhir+xml;fhirVersion=4.0;charset=utf-8",
          "application/fhir+xml;fhirVersion=4.0",
          "application/fhir+xml;charset=utf-8",
          "application/fhir+xml",
          "APPLICATION/FHIR+XML");

  private final List<String> unknownTypes =
      List.of(
          "*/*",
          "application/json",
          "application/xml",
          "application/vnd.openxmlformats-officedocument.presentationml.presentation",
          "text/html",
          "text/xml",
          "text/plain",
          "charset=utf-8;application/fhir+json",
          "charset=utf-8;application/fhir+xml");

  @Test
  void fromStringJson() {
    for (val s : jsonTypes) {
      val expected = MediaType.FHIR_JSON;
      val actual = MediaType.fromString(s);
      assertEquals(expected, actual);
    }
  }

  @Test
  void fromStringXml() {
    for (val s : xmlTypes) {
      val expected = MediaType.FHIR_XML;
      val actual = MediaType.fromString(s);
      assertEquals(expected, actual);
    }
  }

  @Test
  void fromStringUnknown() {
    for (val s : unknownTypes) {
      val expected = MediaType.UNKNOWN;
      val actual = MediaType.fromString(s);
      assertEquals(expected, actual);
    }
  }

  @Test
  void fromStringEmpty() {
    val inputs = Arrays.asList("", null);
    inputs.forEach(
        s -> {
          val expected = MediaType.EMPTY;
          val actual = MediaType.fromString(s);
          assertEquals(expected, actual);
        });
  }

  @Test
  void comparisonJson() {
    jsonTypes.forEach(
        typeString -> {
          val expectedPlain = MediaType.FHIR_JSON;
          val expectedAccept = MediaType.ACCEPT_FHIR_JSON;
          val actual = MediaType.fromString(typeString);
          assertTrue(actual.isEquivalentTo(expectedPlain));
          assertTrue(actual.isEquivalentTo(expectedAccept));
        });
  }

  @Test
  void comparisonXml() {
    xmlTypes.forEach(
        typeString -> {
          val expectedPlain = MediaType.FHIR_XML;
          val expectedAccept = MediaType.ACCEPT_FHIR_XML;
          val actual = MediaType.fromString(typeString);
          assertTrue(actual.isEquivalentTo(expectedPlain));
          assertTrue(actual.isEquivalentTo(expectedAccept));
        });
  }

  @Test
  void asStringXml() {
    val mt1 = MediaType.ACCEPT_FHIR_XML.asString();
    val exp1 = "application/fhir+xml;q=1.0, application/xml+fhir;q=0.9";
    assertEquals(exp1, mt1);

    val mt2 = MediaType.ACCEPT_FHIR_XML.asString(true);
    val exp2 = "application/fhir+xml";
    assertEquals(exp2, mt2);

    val mt3 = MediaType.ACCEPT_FHIR_XML.asString(true, true);
    val exp3 = "fhir+xml";
    assertEquals(exp3, mt3);
  }

  @Test
  void asStringJson() {
    val mt1 = MediaType.ACCEPT_FHIR_JSON.asString();
    val exp1 = "application/fhir+json;q=1.0, application/json+fhir;q=0.9";
    assertEquals(exp1, mt1);

    val mt2 = MediaType.ACCEPT_FHIR_JSON.asString(true);
    val exp2 = "application/fhir+json";
    assertEquals(exp2, mt2);

    val mt3 = MediaType.ACCEPT_FHIR_JSON.asString(true, true);
    val exp3 = "fhir+json";
    assertEquals(exp3, mt3);
  }

  @Test
  void toFhirEncodingJson() {
    val mediaTypes = List.of(MediaType.FHIR_JSON, MediaType.ACCEPT_FHIR_JSON);
    mediaTypes.forEach(
        mt -> {
          val encoding = mt.toFhirEncoding();
          assertEquals(EncodingType.JSON, encoding);
        });
  }

  @Test
  void toFhirEncodingXml() {
    val mediaTypes = List.of(MediaType.FHIR_XML, MediaType.ACCEPT_FHIR_XML);
    mediaTypes.forEach(
        mt -> {
          val encoding = mt.toFhirEncoding();
          assertEquals(EncodingType.XML, encoding);
        });
  }

  @Test
  void shouldThrowOnInvalidFhirEncoding() {
    val mediaType = List.of(MediaType.EMPTY, MediaType.UNKNOWN);
    mediaType.forEach(mt -> assertThrows(UnsupportedEncodingException.class, mt::toFhirEncoding));
  }
}
