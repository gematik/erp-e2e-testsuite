/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fhir.values.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import de.gematik.bbriccs.utils.ResourceLoader;
import java.util.Set;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@SuppressWarnings("java:S106")
@Slf4j
public class CommunicationPayloadValidation {

  private final JsonNode schemaNode;
  private final ObjectMapper mapper;

  @SneakyThrows
  public CommunicationPayloadValidation(String pathToProfile) {
    val schemaJson = ResourceLoader.readFileFromResource(pathToProfile);
    mapper = new ObjectMapper();
    schemaNode = mapper.readTree(schemaJson);
  }

  public Set<ValidationMessage> validate(Object toValidate) {

    JsonNode objectNode = mapper.valueToTree(toValidate);

    var factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
    var jsonSchema = factory.getSchema(schemaNode);

    Set<ValidationMessage> errors = jsonSchema.validate(objectNode);
    errors.forEach(e -> log.info(e.getMessage()));
    return errors;
  }

  public boolean isValid(Object toValidate) {
    return validate(toValidate).isEmpty();
  }
}
