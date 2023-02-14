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

package de.gematik.test.erezept.actions;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import de.gematik.test.fuzzing.core.*;
import java.nio.file.*;
import java.util.*;
import lombok.*;
import net.serenitybdd.core.*;
import net.serenitybdd.core.steps.*;
import net.serenitybdd.screenplay.*;
import net.thucydides.core.annotations.*;

@RequiredArgsConstructor
public class ActivatePrescription extends ErpAction<ErxTask> {

  private final String taskId;
  private final AccessCode accessCode;
  private final KbvErpBundle kbvBundle;

  private final List<StringMutator> stringBundleMutators;
  private final List<ByteArrayMutator> signedBundleMutators;

  public static Builder forGiven(ErpInteraction<ErxTask> interaction) {
    return forGiven(interaction.getExpectedResponse());
  }

  public static Builder forGiven(ErxTask task) {
    return withId(task.getUnqualifiedId()).andAccessCode(task.getAccessCode());
  }

  public static Builder withId(String taskId) {
    return new Builder(taskId);
  }

  @Override
  @Step("{0} aktiviert Task #taskId mit #accessCode und #kbvBundle")
  public ErpInteraction<ErxTask> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);

    var encodedKbv = erpClient.encode(kbvBundle, erpClient.getSendMime().toFhirEncoding());
    for (val fuzzingStep : stringBundleMutators) {
      encodedKbv = fuzzingStep.apply(encodedKbv);
    }

    val signedKbv = konnektor.signDocumentWithHba(encodedKbv).getPayload();
    signedBundleMutators.forEach(m -> m.accept(signedKbv));

    val cmd = new TaskActivateCommand(taskId, accessCode, signedKbv);
    val interaction = this.performCommandAs(cmd, actor);

    if (interaction.isOfExpectedType()) {
      val task = interaction.getExpectedResponse();
      val dmc = DmcPrescription.ownerDmc(task.getUnqualifiedId(), task.getAccessCode());
      writeDmcToReport(dmc);
    }

    return interaction;
  }

  @SneakyThrows
  private void writeDmcToReport(DmcPrescription dmc) {
    // write the DMC to file and append to the Serenity Report
    val dmcPath =
        Path.of("target", "site", "serenity", "dmcs", format("dmc_{0}.png", dmc.getTaskId()));
    DataMatrixCodeGenerator.writeToFile(dmc.getTaskId(), dmc.getAccessCode(), dmcPath.toFile());

    Serenity.recordReportData()
        .withTitle("Data Matrix Code for " + dmc.getTaskId())
        .downloadable()
        .fromFile(dmcPath);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final String taskId;
    private final List<StringMutator> stringMutators = new LinkedList<>();
    private final List<ByteArrayMutator> signedBundleMutators = new LinkedList<>();
    private AccessCode accessCode;

    public Builder andAccessCode(AccessCode accessCode) {
      this.accessCode = accessCode;
      return this;
    }

    public Builder withStringMutator(List<StringMutator> manipulator) {
      this.stringMutators.addAll(manipulator);
      return this;
    }

    public Builder withStringMutator(StringMutator manipulator) {
      this.stringMutators.add(manipulator);
      return this;
    }

    public Builder withByteArrayMutator(List<ByteArrayMutator> manipulator) {
      this.signedBundleMutators.addAll(manipulator);
      return this;
    }

    public Builder withByteArrayMutator(ByteArrayMutator manipulator) {
      this.signedBundleMutators.add(manipulator);
      return this;
    }

    public ActivatePrescription withKbvBundle(KbvErpBundle bundle) {
      Object[] params = {taskId, accessCode, bundle, stringMutators, signedBundleMutators};
      return new Instrumented.InstrumentedBuilder<>(ActivatePrescription.class, params)
          .newInstance();
    }
  }
}
