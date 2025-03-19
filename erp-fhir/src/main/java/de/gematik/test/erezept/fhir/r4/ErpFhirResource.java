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

package de.gematik.test.erezept.fhir.r4;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.util.IdentifierUtil;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;

/**
 * This interface defines further custom methods which might be required when dealing generically
 * with custom HAPI Resources in {@link de.gematik.test.erezept.fhir.r4}
 */
public interface ErpFhirResource extends IAnyResource {

  String getDescription();

  default Reference asReference() {
    val resourceType =
        Optional.ofNullable(this.getClass().getAnnotation(ResourceDef.class))
            .map(ResourceDef::name)
            .orElseThrow(
                () ->
                    new BuilderException(
                        format(
                            "ResourceDef annotation not found for class {0}",
                            this.getClass().getSimpleName())));
    return createReference(resourceType, getId());
  }

  static Reference createReference(ResourceType resourceType, String id) {
    return createReference(resourceType.name(), id);
  }

  static Reference createReference(String resourceType, String id) {
    val unqualifiedId = IdentifierUtil.getUnqualifiedId(id);
    val referenceValue = format("{0}/{1}", resourceType, unqualifiedId);
    return new Reference(referenceValue);
  }
}
