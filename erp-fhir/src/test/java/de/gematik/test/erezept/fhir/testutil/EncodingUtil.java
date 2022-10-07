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

package de.gematik.test.erezept.fhir.testutil;

import static java.text.MessageFormat.format;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class EncodingUtil {

  private EncodingUtil() {
    throw new AssertionError();
  }

  public static EncodingType flipEncoding(EncodingType orig) {
    EncodingType ret;
    if (orig == EncodingType.XML) {
      ret = EncodingType.JSON;
    } else {
      ret = EncodingType.XML;
    }
    return ret;
  }

  /**
   * Do the Round-Trip:
   *
   * <ul>
   *   <li>take file from given <code>filepath</code> and validate/parse with given <code>parser
   *       </code> to <code>clazz</code>
   *   <li>decode to <b>original Encoding</b> and validate result
   *   <li>decode to <b>opposite Encoding</b> and validate result
   * </ul>
   *
   * @param parser which will be used to validate/parse/encode
   * @param filepath is the path to a file which contains the encoded FHIR Resource
   * @param clazz is Class which represents the content within original file
   * @return the given Resource from <code>filepath</code> with flipped encoding
   */
  public static String validateRoundtripEncoding(
      FhirParser parser, String filepath, Class<? extends Resource> clazz) {
    // read and validate the original content from file
    val originalContent = ResourceUtils.readFileFromResource(filepath);
    val originalValidationResult = parser.validate(originalContent);
    assertTrue(originalValidationResult.isSuccessful());
    log.info(
        format(
            "Validation of original File {0} : {1}",
            filepath, originalValidationResult.isSuccessful()));

    // parse the original content to an object
    val originalEncoding = EncodingType.fromString(filepath);
    val originalObject = parser.decode(clazz, originalContent, originalEncoding);
    assertNotNull(
        format("{0} must result in {1}", filepath, clazz.getSimpleName()), originalObject);
    log.info(format("File {0} parsed to {1}", filepath, originalObject));

    // re-encode the original object to its origin encoding and validate again
    val newOriginalContent = reEncode(parser, originalObject, originalEncoding);

    // re-encode the original object to flipped encoding
    val flippedEncoding = EncodingUtil.flipEncoding(originalEncoding);
    val flippedContent = reEncode(parser, originalObject, flippedEncoding);

    return flippedContent;
  }

  private static <T extends IBaseResource> String reEncode(
      FhirParser parser, T resource, EncodingType encoding) {
    val content = parser.encode(resource, encoding);
    val vr = parser.validate(content);

    if (vr.isSuccessful()) {
      log.info(
          format(
              "{0} re-encoded to {1} : {2}",
              resource.getClass().getSimpleName(), encoding, vr.isSuccessful()));
    } else {
      log.error(
          format(
              "Encoding of Resource {0} produces invalid {1} output with {2} errors\n{3}",
              resource.getClass().getSimpleName(),
              encoding,
              vr.getMessages().size(),
              vr.getMessages().stream()
                  .map(
                      svm ->
                          svm.getMessage()
                              + " -> (line: "
                              + svm.getLocationLine()
                              + "; col: "
                              + svm.getLocationCol()
                              + ")")
                  .collect(Collectors.joining("\n"))));
    }

    assertTrue(vr.isSuccessful());

    return content;
  }
}
