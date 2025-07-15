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

package de.gematik.test.erezept.fhir.parser;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.validation.ProfileExtractor;
import de.gematik.bbriccs.fhir.validation.ValidatorFhir;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class FhirParser {

  @Getter private final FhirContext ctx;
  private final ProfileExtractor profileExtractor;
  @Delegate private final ValidatorFhir validator;
  private IParser xmlParser;
  private IParser jsonParser;

  public FhirParser() {
    this.ctx = ProfileFhirParserFactory.createDecoderContext();
    this.profileExtractor = new ProfileExtractor();
    this.validator = ProfileFhirParserFactory.getProfiledValidators();
  }

  public <T extends Resource> T decode(Class<T> expectedClass, String content) {
    val encoding = EncodingType.guessFromContent(content);
    return this.decode(expectedClass, content, encoding);
  }

  public synchronized <T extends Resource> T decode(
      Class<T> expectedClass, String content, EncodingType encoding) {
    val parser = encoding.chooseAppropriateParser(this::getXmlParser, this::getJsonParser);
    return parser.parseResource(expectedClass, fixBeforeDecode(content));
  }

  public Resource decode(String content) {
    val encoding = EncodingType.guessFromContent(content);
    return this.decode(content, encoding);
  }

  public Resource decode(String content, EncodingType encoding) {
    // simply put null as expected class and let HAPI do the mapping
    return this.decode(null, content, encoding);
  }

  public String encode(IBaseResource resource, EncodingType encoding) {
    return encode(resource, encoding, false);
  }

  public synchronized String encode(
      IBaseResource resource, EncodingType encoding, boolean prettyPrint) {
    val parser = encoding.chooseAppropriateParser(this::getXmlParser, this::getJsonParser);
    parser.setPrettyPrint(prettyPrint);
    val encoded = parser.encodeResourceToString(resource);
    return fixEncoded(resource, encoded);
  }

  private IParser getXmlParser() {
    if (this.xmlParser == null) {
      this.xmlParser = ctx.newXmlParser().setOverrideResourceIdWithBundleEntryFullUrl(false);
    }
    return this.xmlParser;
  }

  private IParser getJsonParser() {
    if (this.jsonParser == null) {
      this.jsonParser = ctx.newJsonParser().setOverrideResourceIdWithBundleEntryFullUrl(false);
    }
    return this.jsonParser;
  }

  /**
   * Well, this method is mainly used to fix "the bug" basedOn-References with contained AccessCodes
   * in ErxCommunicationDispReq messages
   *
   * @param resource is the resource to be able to decide if the encoded String needs to be fixed
   * @param encoded is the encoded resource
   * @return the fixed encoded String
   */
  private String fixEncoded(IBaseResource resource, String encoded) {
    String ret = encoded;
    if (resource.getClass().equals(ErxCommunication.class)) {
      ret = encoded.replace("/Task/", "Task/");
    }
    return ret;
  }

  /**
   * Well, this method is mainly used to fix "the bug" basedOn-References with contained AccessCodes
   * in ErxCommunicationDispReq messages
   *
   * @param content is the encoded resource content
   * @return the fixed encoded String
   */
  private String fixBeforeDecode(String content) {
    if (profileExtractor.isUnprofiledSearchSet(content)) {
      return content.replace("\"Task/", "\"/Task/");
    } else {
      // no need to change anything if it's not a searchset collection
      return content;
    }
  }
}
