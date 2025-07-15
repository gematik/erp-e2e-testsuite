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

package de.gematik.test.erezept.fhir.anonymizer;

import com.google.common.reflect.ClassPath;
import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

public class AnonymizerFacade {

  private final AnonymizerContext ctx;

  public AnonymizerFacade() {
    this(AnonymizationType.REPLACING, new CharReplacementStrategy());
  }

  public AnonymizerFacade(AnonymizationType anonymizationType, MaskingStrategy blacker) {
    val anonymizers = this.init();
    this.ctx = new AnonymizerContext(anonymizers, anonymizationType, blacker);
  }

  public <R extends Resource> boolean anonymize(R resource) {
    return this.ctx.anonymize(resource);
  }

  private Map<Class<? extends Resource>, Anonymizer<?>> init() {
    Map<Class<? extends Resource>, Anonymizer<?>> anonymizers = new HashMap<>();
    this.loadAnonymizers().stream()
        .map(this::initClass)
        .forEach(c -> anonymizers.put(c.getType(), c));
    return anonymizers;
  }

  @SneakyThrows
  private Anonymizer<?> initClass(Class<? extends Anonymizer<?>> clazz) {
    return clazz.getConstructor().newInstance();
  }

  private Set<Class<? extends Anonymizer<?>>> loadAnonymizers() {
    val packageName = this.getClass().getPackageName();
    return loadAnonymizers(packageName);
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private Set<Class<? extends Anonymizer<?>>> loadAnonymizers(String packageName) {
    return ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses().stream()
        .filter(clazz -> clazz.getPackageName().equalsIgnoreCase(packageName))
        .filter(clazz -> !clazz.getSimpleName().equalsIgnoreCase(this.getClass().getSimpleName()))
        .map(ClassPath.ClassInfo::load)
        .filter(clazz -> Arrays.stream(clazz.getInterfaces()).toList().contains(Anonymizer.class))
        .map(clazz -> (Class<? extends Anonymizer<?>>) clazz)
        .collect(Collectors.toSet());
  }
}
