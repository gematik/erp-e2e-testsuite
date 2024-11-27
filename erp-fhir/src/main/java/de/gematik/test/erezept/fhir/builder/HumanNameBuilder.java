/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.Hl7StructDef;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.StringType;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HumanNameBuilder {

  private final HumanName.NameUse use;
  private String family;
  private String given;
  private String prefix;

  public static HumanNameBuilder official() {
    return new HumanNameBuilder(HumanName.NameUse.OFFICIAL);
  }

  public HumanNameBuilder given(String given) {
    this.given = given;
    return this;
  }

  public HumanNameBuilder family(String family) {
    this.family = family;
    return this;
  }

  public HumanNameBuilder prefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  public HumanName build() {
    val name = new HumanName();
    name.setUse(this.use);
    name.addGiven(given).setFamily(family);

    if (prefix != null && !prefix.isEmpty()) {
      name.addPrefix(prefix);
      name.getPrefix()
          .get(0)
          .addExtension(Hl7StructDef.ISO_21090_EN_QUALIFIER.getCanonicalUrl(), new CodeType("AC"));
    }
    return name;
  }

  /**
   * Builds the human name for the "new profiles" with additional extensions
   *
   * @return HumanName
   */
  public HumanName buildExt() {
    val name = new HumanName();
    name.setUse(this.use);
    name.addGiven(given).setFamily(family);

    val tokenized = TokenizedFamilyName.split(family);
    name.getFamilyElement()
        .addExtension(
            Hl7StructDef.HUMAN_OWN_NAME.getCanonicalUrl(), new StringType(tokenized.familyName));

    // set prefix if existent
    tokenized
        .getPrefix()
        .ifPresent(
            familyPrefix ->
                name.getFamilyElement()
                    .addExtension(
                        Hl7StructDef.HUMAN_OWN_PREFIX.getCanonicalUrl(),
                        new StringType(familyPrefix)));

    // set name extension if existent
    tokenized
        .getNameExtension()
        .ifPresent(
            nameExtension ->
                name.getFamilyElement()
                    .addExtension(
                        Hl7StructDef.NAMENSZUSATT.getCanonicalUrl(),
                        new StringType(nameExtension)));

    if (prefix != null && !prefix.isEmpty()) {
      name.addPrefix(prefix);
      name.getPrefix()
          .get(0)
          .addExtension(Hl7StructDef.ISO_21090_EN_QUALIFIER.getCanonicalUrl(), new CodeType("AC"));
    }
    return name;
  }

  @RequiredArgsConstructor
  private static class TokenizedFamilyName {
    private static final List<String> KNOWN_PREFIXES = List.of("von", "zu");
    @Nullable private final String nameExtension; // Namenszusatz
    @Nullable private final String prefix;
    @NonNull private final String familyName;

    public Optional<String> getPrefix() {
      return Optional.ofNullable(prefix);
    }

    public Optional<String> getNameExtension() {
      return Optional.ofNullable(nameExtension);
    }

    public static TokenizedFamilyName split(@NonNull String family) {
      if (family.isEmpty()) {
        throw new BuilderException("Given family name is empty");
      }

      val familyTokens = family.split(" ");

      if (familyTokens.length == 1) {
        // family name does not have any additional prefixes or extensions
        return new TokenizedFamilyName(null, null, family);
      } else if (familyTokens.length == 2) {
        val familyName = familyTokens[1]; // last element is usually the family name
        if (KNOWN_PREFIXES.contains(familyTokens[0])) {
          // first element seams to be a prefix
          return new TokenizedFamilyName(null, familyTokens[0], familyName);
        } else {
          // first element is not a known prefix, as this one as name extension
          return new TokenizedFamilyName(familyTokens[0], null, familyName);
        }
      } else {
        // more than 2 tokens!
        val familyName =
            familyTokens[familyTokens.length - 1]; // last element is usually the family name
        val nameExtension = familyTokens[0]; // first element is usually the name extension
        val prefixTokens = Arrays.copyOfRange(familyTokens, 1, familyTokens.length - 1);
        val prefix = String.join(" ", prefixTokens);
        return new TokenizedFamilyName(nameExtension, prefix, familyName);
      }
    }
  }
}
