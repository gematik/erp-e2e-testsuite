/*
 * Copyright (c) 2023 gematik GmbH
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
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.fhir.parser.profiles.FhirProfiledValidator;
import de.gematik.test.erezept.fhir.parser.profiles.ProfileExtractor;
import de.gematik.test.erezept.fhir.parser.profiles.ProfileFhirParserFactory;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class FhirParser {

  @Getter private final FhirContext ctx;
  private final List<FhirProfiledValidator> validators;
  private final FhirProfiledValidator defaultProfileValidator;
  private final @Nullable FhirValidator genericValidator;
  private IParser xmlParser;
  private IParser jsonParser;

  public FhirParser() {
    this.ctx = ProfileFhirParserFactory.createDecoderContext();
    this.validators = ProfileFhirParserFactory.getProfiledValidators();
    this.defaultProfileValidator = this.validators.get(0);
    this.genericValidator = ProfileFhirParserFactory.createGenericFhirValidator(this.ctx);
  }

  /**
   * Check beforehand if the given content is valid
   *
   * @param content to be validated
   * @return successful ValidationResult if content represents a valid FHIR-Resource and a
   *     unsuccessful ValidationResult otherwise
   */
  public ValidationResult validate(@NonNull final String content) {
    if (ProfileExtractor.isSearchSetOrCollection(content)) {
      val bundle = decode(Bundle.class, content);
      return validate(bundle);
    } else {
      val p = chooseProfileValidator(() -> ProfileExtractor.extractProfile(content));
      return p.validate(content);
    }
  }

  public ValidationResult validate(@NonNull final Bundle bundle) {
    // 1. validate the whole bundle without any profiles
    val validationMessages = new LinkedList<SingleValidationMessage>();
    if (this.genericValidator != null) {
      val vrBundle = this.genericValidator.validateWithResult(bundle);
      validationMessages.addAll(vrBundle.getMessages());
    }

    // 2. now validate each entry with a profiled validator
    bundle
        .getEntry()
        .forEach(
            entry -> {
              val resource = entry.getResource();

              // this hack here is required because HAPI uses here still the fullUrl from the entry
              // by splitting the fullUrl and taking the last part we get the correct ID of this
              // resource
              val originalId = resource.getId();
              val idTokens = originalId.split("[/|:]");
              val idPart = idTokens[idTokens.length - 1];
              resource.setId(idPart);

              val profile = resource.getMeta().getProfile();
              val profileUrl = profile.isEmpty() ? "" : profile.get(0).asStringValue();
              val validator = chooseProfileValidator(profileUrl);
              val result = validator.validate(resource);

              validationMessages.addAll(result.getMessages());
            });
    return new ValidationResult(this.getCtx(), validationMessages);
  }

  public boolean isValid(@NonNull final String content) {
    return validate(content).isSuccessful();
  }

  public boolean isValid(@NonNull final Bundle bundle) {
    return validate(bundle).isSuccessful();
  }

  public <T extends Resource> T decode(Class<T> expectedClass, @NonNull final String content) {
    val encoding = EncodingType.guessFromContent(content);
    return this.decode(expectedClass, content, encoding);
  }

  public synchronized <T extends Resource> T decode(
      Class<T> expectedClass, @NonNull final String content, EncodingType encoding) {
    val parser = encoding.chooseAppropriateParser(this::getXmlParser, this::getJsonParser);
    return parser.parseResource(expectedClass, fixBeforeDecode(content));
  }

  public Resource decode(@NonNull final String content) {
    val encoding = EncodingType.guessFromContent(content);
    return this.decode(content, encoding);
  }

  public Resource decode(@NonNull final String content, EncodingType encoding) {
    // simply put null as expected class and let HAPI do the mapping
    return this.decode(null, content, encoding);
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
          val defaultParser = this.defaultProfileValidator;
          log.debug(
              format(
                  "Could not determine the Profile from given content! Use default parser ''{0}''",
                  defaultParser.getName()));
          chosenParser.set(defaultParser);
        });

    val chosenValidator = chosenParser.get();
    val profileUrl = profileUrlOpt.orElse("no found profile");
    log.debug(format("Choose Validator {0} for {1}", chosenValidator.getName(), profileUrl));
    return chosenValidator;
  }

  private FhirProfiledValidator chooseProfileValidator(String profileUrl) {
    val parserOpt = this.validators.stream().filter(p -> p.doesSupport(profileUrl)).findFirst();

    if (parserOpt.isPresent()) {
      log.debug(format("Use Parser ''{0}'' for {1}", parserOpt.get().getName(), profileUrl));
    } else {
      log.debug(
          format(
              "No supporting Parser found for {0}, use Parser ''{1}'' as default",
              profileUrl, this.defaultProfileValidator.getName()));
    }

    return parserOpt.orElse(this.defaultProfileValidator);
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

  /**
   * Well, this method is mainly used to fix "the bug" basedOn-References with contained AccessCodes
   * in ErxCommunicationDispReq messages
   *
   * @param content is the encoded resource content
   * @return the fixed encoded String
   */
  private String fixBeforeDecode(String content) {
    if (ProfileExtractor.isSearchSetOrCollection(content)) {
      return content.replace("\"Task/", "\"/Task/");
    } else {
      // no need to change anything if it's not a searchset collection
      return content;
    }
  }
}
