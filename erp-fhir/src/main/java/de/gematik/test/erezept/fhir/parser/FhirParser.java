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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Configuration;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r5.utils.IResourceValidator;

@Slf4j
public class FhirParser {

  private final FhirContext ctx;
  private final FhirValidator hapiValidator;

  public FhirParser() {
    this.ctx = FhirContext.forR4();

    this.ctx.setParserErrorHandler(new StrictErrorHandler());
    Configuration.setAcceptInvalidEnums(true);

    this.hapiValidator = ctx.newValidator();

    // create support chain for validation
    val supportChain = new ArrayList<IValidationSupport>();
    supportChain.add(new DefaultProfileValidationSupport(this.ctx));
    supportChain.add(new InMemoryTerminologyServerValidationSupport(this.ctx));
    supportChain.add(new SnapshotGeneratingValidationSupport(this.ctx));
    supportChain.add(new CustomProfileSupport(this.ctx));

    // configure the HAPI FhirParser
    val fiv = new FhirInstanceValidator(this.ctx);
    val support = new ValidationSupportChain(supportChain.toArray(IValidationSupport[]::new));

    fiv.setValidationSupport(support);
    fiv.setNoTerminologyChecks(true);
    fiv.setAssumeValidRestReferences(false);

    fiv.setBestPracticeWarningLevel(IResourceValidator.BestPracticeWarningLevel.Hint);
    hapiValidator.registerValidatorModule(fiv);
    hapiValidator.registerValidatorModule(new ErrorMessageFilter());
  }

  /**
   * Check beforehand if the given content is valid
   *
   * @param content to be validated
   * @return true if content represents a valid FHIR-Resource and false otherwise
   */
  public ValidationResult validate(final String content) {
    ValidationResult ret;
    try {
      ret = this.hapiValidator.validateWithResult(content);
    } catch (Exception e) {
      /*
      some sort of error led to an Exception: handle this case via ValidationResult=ERROR
       */
      log.error("Error while validating FHIR content");
      val svm = new SingleValidationMessage();
      svm.setMessage(e.getMessage());
      svm.setSeverity(ResultSeverityEnum.ERROR);
      ret = new ValidationResult(this.ctx, List.of(svm));
    }
    return ret;
  }

  public boolean isValid(final String content) {
    return this.validate(content).isSuccessful();
  }

  public <T extends Resource> T decode(Class<T> expectedClass, @NonNull final String content) {
    val encoding = EncodingType.guessFromContent(content);
    return this.decode(expectedClass, content, encoding);
  }

  public <T extends Resource> T decode(
      Class<T> expectedClass, @NonNull final String content, EncodingType encoding) {
    IParser parser =
        encoding.chooseAppropriateParser(this.ctx::newXmlParser, this.ctx::newJsonParser);
    return parser.parseResource(expectedClass, content);
  }

  public Resource decode(@NonNull final String content) {
    return this.decode(Resource.class, content);
  }

  public Resource decode(@NonNull final String content, EncodingType encoding) {
    return this.decode(Resource.class, content, encoding);
  }

  public String encode(IBaseResource resource, EncodingType encoding) {
    return encode(resource, encoding, false);
  }

  public String encode(IBaseResource resource, EncodingType encoding, boolean prettyPrint) {
    IParser parser =
        encoding.chooseAppropriateParser(this.ctx::newXmlParser, this.ctx::newJsonParser);

    parser.setPrettyPrint(prettyPrint);

    val encoded = parser.encodeResourceToString(resource);
    return fixEncoded(resource, encoded);
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
