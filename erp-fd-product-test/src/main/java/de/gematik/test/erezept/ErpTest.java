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

package de.gematik.test.erezept;

import de.gematik.bbriccs.toggle.FeatureConfiguration;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.exceptions.NotAnActorException;
import de.gematik.test.core.extensions.ErpTestExtension;
import de.gematik.test.erezept.actors.ActorStage;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.actors.KtrActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.config.exceptions.ConfigurationMappingException;
import de.gematik.test.erezept.toggle.CucumberFeatureParser;
import de.gematik.test.erezept.toggle.CucumberFeatureToggle;
import java.lang.reflect.Field;
import java.util.Arrays;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ErpTestExtension.class)
public abstract class ErpTest {

  protected static final FeatureConfiguration featureConf = new FeatureConfiguration();
  protected static final CucumberFeatureParser cucumberFeatures =
      featureConf.getToggle(new CucumberFeatureToggle());

  protected final ActorStage stage;
  protected final ErpFdTestsuiteFactory config;

  protected ErpTest() {
    this.stage = new ActorStage();
    this.config = stage.getConfig();
    instrumentAnnotatedActors();
  }

  /**
   * This method provides a convenient way of retrieving a Doctor by his/her name. This is an
   * alternative to the annotated approach and can be even combined. However, this method might come
   * handy if you need a specific actor only in one single Testcase within an ErpTest. By using this
   * method you can save on unnecessary instantiation/initialisation of the actor in Testcases where
   * the actor is not required.
   *
   * <p>In contrast, if you need the actor throughout all/most Testcases within an ErpTest it should
   * be much easier to use the annotated approach via {@code @Actor(name = "NAME") DoctorActor
   * doctor;}
   *
   * @param name is the name of the actor which will identify the actor from the configuration file
   * @return an initialized DoctorActor or throw a {@link ConfigurationMappingException} if the name
   *     could not be found in the configuration
   */
  public DoctorActor getDoctorNamed(String name) {
    return stage.getDoctorNamed(name);
  }

  /**
   * This method provides a convenient way of retrieving a Patient by his/her name. This is an
   * alternative to the annotated approach and can be even combined. However, this method might come
   * handy if you need a specific actor only in one single Testcase within an ErpTest. By using this
   * method you can save on unnecessary instantiation/initialisation of the actor in Testcases where
   * the actor is not required.
   *
   * <p>In contrast, if you need the actor throughout all/most Testcases within an ErpTest it should
   * be much easier to use the annotated approach via {@code @Actor(name = "NAME") PatientActor
   * patient;}
   *
   * @param name is the name of the actor which will identify the actor from the configuration file
   * @return an initialized PatientActor or throw a {@link ConfigurationMappingException} if the
   *     name could not be found in the configuration
   */
  public PatientActor getPatientNamed(String name) {
    return stage.getPatientNamed(name);
  }

  /**
   * This method provides a convenient way of retrieving a Pharmacy by his/her name. This is an
   * alternative to the annotated approach and can be even combined. However, this method might come
   * handy if you need a specific actor only in one single Testcase within an ErpTest. By using this
   * method you can save on unnecessary instantiation/initialisation of the actor in Testcases where
   * the actor is not required.
   *
   * <p>In contrast, if you need the actor throughout all/most Testcases within an ErpTest it should
   * be much easier to use the annotated approach via {@code @Actor(name = "NAME") PharmacyActor
   * pharmacy;}
   *
   * @param name is the name of the actor which will identify the actor from the configuration file
   * @return an initialized PharmacyActor or throw a {@link ConfigurationMappingException} if the
   *     name could not be found in the configuration
   */
  public PharmacyActor getPharmacyNamed(String name) {
    return stage.getPharmacyNamed(name);
  }

  /**
   * This method provides a convenient way of retrieving a KTR by its name. This is an alternative
   * to the annotated approach and can be even combined. However, this method might come handy if
   * you need a specific actor only in one single Testcase within an ErpTest. By using this method
   * you can save on unnecessary instantiation/initialisation of the actor in Testcases where the
   * actor is not required.
   *
   * <p>In contrast, if you need the actor throughout all/most Testcases within an ErpTest it should
   * be much easier to use the annotated approach via {@code @Actor(name = "NAME") PharmacyActor
   * pharmacy;}
   *
   * @param name is the name of the actor which will identify the actor from the configuration file
   * @return an initialized KtrActor or throw a {@link ConfigurationMappingException} if the name
   *     could not be found in the configuration
   */
  public KtrActor getKtrNamed(String name) {
    return stage.getKtrNamed(name);
  }

  /**
   * By instrumenting actors annotated with @Actor, the author of the concrete testscenario does not
   * need to care about the actors. Haven a member annotated with @Actor is enough
   */
  private void instrumentAnnotatedActors() {
    val actorFields =
        Arrays.stream(this.getClass().getDeclaredFields())
            .filter(field -> field.getAnnotation(Actor.class) != null)
            .toList();

    actorFields.forEach(this::instrumentActorField);
  }

  /**
   * Why does an Actor needs to be instrumented rather than directly instantiated? The main reason
   * is Serenity. Serenity requires an instrumented Object to be able to report methods annotated
   * with "@Step" within the object. Serenity does also have its own Annotation and "instrumentation
   * hooks" for that: the "@Steps" (note the plural!)
   *
   * <p>However, this Serenity-native mechanism does not know our actors (or any concrete "Steps" in
   * general) and requires an empty constructor. By using our own annotation "@Actor" we achieve
   * three main goals: - our steps are apparent as Actors, visually and semantically - we can
   * provide our configure directly via the constructor - we have full control over the mechanism of
   * instrumenting the actors
   *
   * @param field of a concrete test scenario actor
   */
  @SneakyThrows
  @SuppressWarnings({"java:S3011", "unchecked"})
  private void instrumentActorField(Field field) {
    val name = field.getAnnotation(Actor.class).name();

    Class<? extends ErpActor> actorType;
    if (!field.getType().getSuperclass().equals(ErpActor.class)) {
      throw new NotAnActorException(field);
    } else {
      actorType = (Class<? extends ErpActor>) field.getType();
    }

    val actor = stage.instrumentNewActor(actorType, name);

    field.setAccessible(true); // well, no other ways to access the actor -> suppress java:S3011
    field.set(this, actor);
    field.setAccessible(false);
  }
}
