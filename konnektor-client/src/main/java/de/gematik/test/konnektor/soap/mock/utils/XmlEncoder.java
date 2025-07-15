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

package de.gematik.test.konnektor.soap.mock.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class XmlEncoder {

  public static <T> T parse(Class<T> clazz, String input) throws JAXBException, IOException {
    return parse(clazz, input.getBytes(StandardCharsets.UTF_8));
  }

  public static <T> T parse(Class<T> clazz, byte[] input) throws JAXBException, IOException {
    byte[] decoded = Base64.getDecoder().decode(input);
    val reader = new StringReader(decompress(decoded));
    val context = JAXBContext.newInstance(clazz);
    return (T) context.createUnmarshaller().unmarshal(reader);
  }

  @SneakyThrows
  public static <T> String asXml(T obj) {
    val ret = new StringWriter();
    val jaxbContext = JAXBContext.newInstance(obj.getClass());
    val jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    jaxbMarshaller.marshal(obj, ret);
    return ret.toString();
  }

  @SneakyThrows
  public static <T> String encode(T obj) {
    val compressed = compress(asXml(obj).getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(compressed);
  }

  private static byte[] compress(byte[] data) throws IOException {
    try (val baos = new ByteArrayOutputStream();
        val gzipOut = new GZIPOutputStream(baos)) {
      gzipOut.write(data);
      gzipOut.finish();
      return baos.toByteArray();
    }
  }

  private static String decompress(byte[] data) throws IOException {
    val ret = new StringBuilder();
    try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data))) {
      ret.append(new String(in.readAllBytes()));
    }
    return ret.toString();
  }
}
