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

package de.gematik.test.erezept.lei;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static de.gematik.test.erezept.lei.StepsArchUnitTest.ValidRegexStepDefinition.haveValidStepDefinitions;
import static java.text.MessageFormat.format;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import io.cucumber.java.de.*;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.val;
import org.junit.jupiter.api.Test;

class StepsArchUnitTest {

  @Test
  void shouldHaveValidRegexStepDefinitionAnnotations() {
    val steps = new ClassFileImporter().importPackages("de.gematik.test.erezept.lei.steps");
    val rule =
        methods()
            .that()
            .areAnnotatedWith(Angenommen.class)
            .or()
            .areAnnotatedWith(Wenn.class)
            .or()
            .areAnnotatedWith(Dann.class)
            .or()
            .areAnnotatedWith(Und.class)
            .or()
            .areAnnotatedWith(Aber.class)
            .should(haveValidStepDefinitions());

    rule.check(steps);
  }

  public static class ValidRegexStepDefinition extends ArchCondition<JavaMethod> {

    public ValidRegexStepDefinition(Object... args) {
      super("Should have only valid Regex Steps-Definitions", args);
    }

    public static ValidRegexStepDefinition haveValidStepDefinitions() {
      return new ValidRegexStepDefinition();
    }

    @Override
    public void check(JavaMethod step, ConditionEvents conditionEvents) {
      val stepDefinitions =
          step.getAnnotations().stream()
              .filter(
                  a ->
                      a.getType()
                          .getName()
                          .startsWith("io.cucumber.java")) // filter for cucumber annotations
              //              .map(a -> a.get("value"))
              .collect(Collectors.toList());

      // check if
      stepDefinitions.forEach(
          annotation -> {
            val value = annotation.get("value");
            value.ifPresentOrElse(
                regex ->
                    conditionEvents.add(
                        checkStepDefinitionValue(step, annotation.getType(), (String) regex)),
                () -> conditionEvents.add(emptyDefinitionCondition(step, annotation.getType())));
          });
    }

    private ConditionEvent emptyDefinitionCondition(JavaMethod step, JavaType type) {
      return SimpleConditionEvent.violated(
          step,
          format(
              "Step {0} with Annotation {1} does not have value", step.getName(), type.getName()));
    }

    private ConditionEvent checkStepDefinitionValue(JavaMethod step, JavaType type, String value) {
      if (value == null || value.isEmpty()) {
        return emptyDefinitionCondition(step, type);
      }

      val errors = validate(step, value);
      if (errors.isEmpty()) {
        return SimpleConditionEvent.satisfied(
            step, format("Step {0} has valid Stepdefinition", step.getName()));
      } else {
        val errorMessage =
            format(
                "Step {0} with Annotation {1} has invalid Stepdefinition: {2}\n\t- {3}",
                step.getName(), type.getName(), value, String.join("\n\t- ", errors));
        return SimpleConditionEvent.violated(step, errorMessage);
      }
    }

    private List<String> validate(JavaMethod step, String value) {
      val ret = new LinkedList<String>();
      if (!value.startsWith("^")) {
        ret.add("Each Stepdefinition MUST start with '^' character");
      }

      if (!value.endsWith("$")) {
        ret.add("Each Stepdefinition MUST end with '$' character");
      }

      val stepMethod = step.reflect();
      if (!stepMethod.getReturnType().getName().equals("void")) {
        ret.add(
            format(
                "Stepdefinition-Methods MUST have return type void but found {1}",
                step.reflect().getReturnType().getName()));
      }

      val modifier = stepMethod.getModifiers();
      if (!Modifier.isPublic(modifier)) {
        ret.add(format("Stepdefinition-Method MUST be public"));
      }
      if (Modifier.isStatic(modifier)) {
        ret.add(format("Stepdefinition-Method MUST NOT be static"));
      }

      val pattern = Pattern.compile(value);
      val matcher = pattern.matcher(value);
      val regexParameters = matcher.groupCount();
      val methodParameters =
          step.getParameters().stream()
              .filter(
                  p ->
                      !p.getType()
                          .getName()
                          .contains("DataTable")) // DataTables are not within the RegEx
              .count();

      if (regexParameters != methodParameters) {
        ret.add(
            format(
                "Stepdefinition has {0} parameters but the method has {1}",
                regexParameters, methodParameters));
      }

      return ret;
    }
  }
}
