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

package de.gematik.test.erezept.client.usecases;

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.builder.erp.FlowTypeBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class TaskCreateCommand extends BaseCommand<ErxTask> {

  private final PrescriptionFlowType flowType;

  /** Create a Task with the "default" FlowType 160 */
  public TaskCreateCommand() {
    this(PrescriptionFlowType.FLOW_TYPE_160);
  }

  public TaskCreateCommand(PrescriptionFlowType flowType) {
    super(ErxTask.class, HttpRequestMethod.POST, "Task");
    this.flowType = flowType;
  }

  /**
   * This method returns the last (tailing) part of the URL of the inner-HTTP Request e.g.
   * /Task/[id] or /Communication?[queryParameter]
   *
   * @return the tailing part of the URL which combines to full URL like [baseUrl][tailing Part]
   */
  @Override
  public String getRequestLocator() {
    return this.getResourcePath() + "/$create";
  }

  public Optional<Resource> getRequestBody() {
    return Optional.of(FlowTypeBuilder.build(flowType));
  }
}
