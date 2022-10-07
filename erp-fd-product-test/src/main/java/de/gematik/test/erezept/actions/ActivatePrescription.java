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

package de.gematik.test.erezept.actions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.DataMatrixCodeGenerator;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.fuzzing.core.ByteArrayMutator;
import de.gematik.test.fuzzing.core.StringMutator;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.thucydides.core.annotations.Step;

@RequiredArgsConstructor
public class ActivatePrescription extends ErpAction<ErxTask> {

  private final String taskId;
  private final AccessCode accessCode;
  private final KbvErpBundle kbvBundle;

  private final List<StringMutator> stringBundleMutators;
  private final List<ByteArrayMutator> signedBundleMutators;

  @Override
  @Step("{0} aktiviert Task #taskId mit #accessCode und #kbvBundle")
  public ErpInteraction<ErxTask> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);

    var encodedKbv = erpClient.encode(kbvBundle, erpClient.getSendMime().toFhirEncoding());
    for (val fuzzingStep : stringBundleMutators) {
      encodedKbv = fuzzingStep.apply(encodedKbv);
    }

    val signedKbv = konnektor.signDocument(encodedKbv);
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

  public static Builder forGiven(ErpInteraction<ErxTask> interaction) {
    return forGiven(interaction.getExpectedResponse());
  }

  public static Builder forGiven(ErxTask task) {
    return withId(task.getUnqualifiedId()).andAccessCode(task.getAccessCode());
  }

  public static Builder withId(String taskId) {
    return new Builder(taskId);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final String taskId;
    private AccessCode accessCode;
    private final List<StringMutator> stringMutators = new LinkedList<>();
    private final List<ByteArrayMutator> signedBundleMutators = new LinkedList<>();

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
