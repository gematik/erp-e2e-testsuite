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

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import java.util.LinkedList;
import java.util.List;
import org.hl7.fhir.r4.model.Extension;

public abstract class KbvErpMedicationBaseBuilder<B extends KbvErpMedicationBaseBuilder<B>>
    extends ResourceBuilder<KbvErpMedication, B> {

  protected final List<Extension> extensions = new LinkedList<>();

  protected KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  protected MedicationCategory category = MedicationCategory.C_00;
  protected boolean isVaccine = false;

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public B version(KbvItaErpVersion version) {
    this.kbvItaErpVersion = version;
    return self();
  }

  public B category(MedicationCategory category) {
    this.category = category;
    return self();
  }

  public B isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return self();
  }
}
