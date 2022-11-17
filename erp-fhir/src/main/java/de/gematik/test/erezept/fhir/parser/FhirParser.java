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

package de.gematik.test.erezept.fhir.parser;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.fhir.parser.profiles.FhirProfiledValidator;
import de.gematik.test.erezept.fhir.parser.profiles.ProfileExtractor;
import de.gematik.test.erezept.fhir.parser.profiles.ProfileFhirParserFactory;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class FhirParser {

  private final FhirContext ctx;
  private IParser xmlParser;
  private IParser jsonParser;
  private final List<FhirProfiledValidator> validators;

  public FhirParser() {
    this.ctx = FhirContext.forR4();
    this.validators = ProfileFhirParserFactory.getProfiledValidators();
  }

  /**
   * Check beforehand if the given content is valid
   *
   * @param content to be validated
   * @return true if content represents a valid FHIR-Resource and false otherwise
   */
  public ValidationResult validate(@NonNull final String content) {
    val p = chooseProfileValidator(() -> ProfileExtractor.extractProfile(content));
    return p.validate(content);
  }

  public boolean isValid(@NonNull final String content) {
    val p = chooseProfileValidator(() -> ProfileExtractor.extractProfile(content));
    return p.isValid(content);
  }

  public <T extends Resource> T decode(Class<T> expectedClass, @NonNull final String content) {
    val encoding = EncodingType.guessFromContent(content);
    return this.decode(expectedClass, content, encoding);
  }

  public synchronized <T extends Resource> T decode(
      Class<T> expectedClass, @NonNull final String content, EncodingType encoding) {
    val parser = encoding.chooseAppropriateParser(ctx::newXmlParser, ctx::newJsonParser);
    return parser.parseResource(expectedClass, content);
  }

  public Resource decode(@NonNull final String content) {
    return this.decode(Resource.class, content);
  }

  public Resource decode(@NonNull final String content, EncodingType encoding) {
    return this.decode(Resource.class, content, encoding);
  }

  public String encode(@NonNull IBaseResource resource, EncodingType encoding) {
    return encode(resource, encoding, false);
  }

  public synchronized String encode(
      @NonNull IBaseResource resource, EncodingType encoding, boolean prettyPrint) {
    val parser = encoding.chooseAppropriateParser(this::getXmlParser, this::getJsonParser);
    parser.setPrettyPrint(prettyPrint);
    val encoded = parser.encodeResourceToString(resource);
    return fixEncoded(resource, encoded);
  }

  private IParser getXmlParser() {
    if (this.xmlParser == null) {
      this.xmlParser = ctx.newXmlParser();
    }
    return this.xmlParser;
  }

  private IParser getJsonParser() {
    if (this.jsonParser == null) {
      this.jsonParser = ctx.newJsonParser();
    }
    return this.jsonParser;
  }

  private FhirProfiledValidator chooseProfileValidator(Supplier<Optional<String>> profileSupplier) {
    val profileUrlOpt = profileSupplier.get();
    AtomicReference<FhirProfiledValidator> chosenParser = new AtomicReference<>();
    profileUrlOpt.ifPresentOrElse(
        url -> chosenParser.set(chooseProfileValidator(url)),
        () -> {
          val defaultParser = this.validators.get(0);
          log.debug(
              format(
                  "Could not determine the Profile from given content! Use default parser ''{0}''",
                  defaultParser.getName()));
          chosenParser.set(defaultParser);
        });

    return chosenParser.get();
  }

  private FhirProfiledValidator chooseProfileValidator(String profileUrl) {
    val parserOpt = this.validators.stream().filter(p -> p.doesSupport(profileUrl)).findFirst();

    if (parserOpt.isPresent()) {
      log.debug(format("Use Parser ''{0}'' for {1}", parserOpt.get().getName(), profileUrl));
    } else {
      log.debug(
          format(
              "No supporting Parser found for {0}, use Parser ''{1}'' as default",
              profileUrl, this.validators.get(0).getName()));
    }

    return parserOpt.orElse(this.validators.get(0));
  }

  /**
   * Well, this method is mainly used to fix "the bug" basedOn-References with contained AccessCodes
   * in ErxCommunicationDispReq messages
   *
   * @param resource is the resource to be able to decide if the encoded String needs to be fixed
   * @param encoded is the encoded resource
   * @return the fixed encoded String
   */
  private String fixEncoded(IBaseResource resource, final String encoded) {
    String ret = encoded;
    if (resource.getClass().equals(ErxCommunication.class)) {
      ret = encoded.replace("/Task/", "Task/");
    }
    return ret;
  }
}
