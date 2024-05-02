/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.anonymizer;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.Hl7StructDef;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@Slf4j
public class AnonymizerContext {

  private final Map<Class<? extends Resource>, Anonymizer<?>> anonymizers;
  private final AnonymizationType anonymizationType;
  private final MaskingStrategy blacker;

  public AnonymizerContext(
      Map<Class<? extends Resource>, Anonymizer<?>> anonymizers,
      AnonymizationType anonymizationType,
      MaskingStrategy blacker) {
    this.anonymizers = anonymizers;
    this.anonymizationType = anonymizationType;
    this.blacker = blacker;
  }

  public AnonymizationType getIdentifierAnonymization() {
    return this.anonymizationType;
  }

  @SuppressWarnings("unchecked")
  public <R extends Resource> boolean anonymize(R resource) {
    val type = (Class<R>) resource.getClass();
    val anonymizer = getTypedAnonymizer(type);
    anonymizer.ifPresent(a -> a.anonymize(this, resource));
    val ret = anonymizer.isPresent();
    log.info("Anonymization of {} {}: {}", type.getSimpleName(), resource.getIdPart(), ret);
    return ret;
  }

  @SuppressWarnings("unchecked")
  private <R extends Resource> Optional<Anonymizer<R>> getTypedAnonymizer(Class<R> forResource) {
    return Optional.ofNullable((Anonymizer<R>) anonymizers.get(forResource));
  }

  public void anonymizeHumanNames(List<HumanName> humanNames) {
    humanNames.forEach(this::anonymize);
  }

  public void anonymizeAddresses(List<Address> addresses) {
    addresses.forEach(this::anonymize);
  }

  public void anonymize(HumanName humanName) {
    humanName.getGiven().forEach(this::anonymize);
    val familyElement = humanName.getFamilyElement();
    val changes = new AtomicInteger();
    familyElement
        .getExtension()
        .forEach(
            ext -> {
              val extTypeValue = ext.getValue();
              val originalValue = extTypeValue.primitiveValue();
              this.anonymize(extTypeValue);
              val anonymizedValue = extTypeValue.primitiveValue();
              familyElement.setValue(
                  familyElement.getValue().replace(originalValue, anonymizedValue));
              changes.getAndIncrement();
            });

    if (changes.get() > 0) {
      setAnonymizedComment(familyElement);
    } else {
      this.anonymize(familyElement);
    }
  }

  public void anonymize(Address address) {
    this.anonymize(address.getCityElement());
    this.anonymize(address.getPostalCodeElement());
    this.anonymize(address.getDistrictElement());
    address
        .getLine()
        .forEach(
            l -> {
              l.getExtension().forEach(ext -> this.anonymize(ext.getValue()));
              val houseNumber = l.getExtensionString(Hl7StructDef.HOUSE_NUMBER.getCanonicalUrl());
              val streetName = l.getExtensionString(Hl7StructDef.STREET_NAME.getCanonicalUrl());
              val addressLine = format("{0} {1}", streetName, houseNumber);
              this.anonymize(l, () -> addressLine);
            });
  }

  public void anonymize(Type input) {
    if (input instanceof StringType st) {
      maskStringType(st);
    } else if (input instanceof DateType dt) {
      anonymize(dt);
    }
  }

  private void anonymize(DateType dt) {
    if (dt != null && dt.hasValue()) {
      setAnonymizedComment(dt);
      dt.setDay(1);
      dt.setMonth(0);
    }
  }

  public void anonymize(StringType input, Supplier<String> valueSupplier) {
    input.setValue(valueSupplier.get());
    setAnonymizedComment(input);
  }

  private void maskStringType(StringType input) {
    if (input != null && input.hasValue()) {
      setAnonymizedComment(input);
      input.setValue(this.blacker.maskString(input.getValue()));
    }
  }

  private void setAnonymizedComment(Type type) {
    type.getFormatCommentsPre().add("Anonymized Value");
  }
}
