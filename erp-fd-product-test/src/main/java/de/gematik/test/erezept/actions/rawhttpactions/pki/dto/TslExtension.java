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

package de.gematik.test.erezept.actions.rawhttpactions.pki.dto;

import eu.europa.esig.trustedlist.jaxb.tsl.TSPServiceInformationType;
import java.util.List;
import org.w3c.dom.Element;

public record TslExtension(String oid, String value) {
  private static TslExtension from(List<Object> elements) {
    return from((Element) elements.get(0), (Element) elements.get(1));
  }

  public static TslExtension from(Element oid, Element value) {
    return new TslExtension(oid.getTextContent(), value.getTextContent());
  }

  public static List<TslExtension> toExtensions(
      TSPServiceInformationType tspServiceInformationType) {
    return tspServiceInformationType.getServiceInformationExtensions().getExtension().stream()
        .filter(
            ex ->
                ex.getContent().get(0) instanceof Element
                    && ex.getContent().get(1) instanceof Element)
        .map(extension -> TslExtension.from(extension.getContent()))
        .toList();
  }
}
