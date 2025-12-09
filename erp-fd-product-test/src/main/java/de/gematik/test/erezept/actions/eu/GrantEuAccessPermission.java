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

package de.gematik.test.erezept.actions.eu;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actors.EuPharmacyActor;
import de.gematik.test.erezept.client.usecases.eu.EuGrantAccessPostCommand;
import de.gematik.test.erezept.fhir.r4.eu.EuAccessPermission;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GrantEuAccessPermission extends ErpAction<EuAccessPermission> {

  private final EuGrantAccessPostCommand euGrantAccessPostCommand;

  public static GrantEuAccessPermissionBuilder withRandomAccessCode() {
    return withAccessCode(EuAccessCode.random());
  }

  public static GrantEuAccessPermissionBuilder withAccessCode(EuAccessCode accessCode) {
    return new GrantEuAccessPermissionBuilder(accessCode);
  }

  @Override
  public ErpInteraction<EuAccessPermission> answeredBy(Actor actor) {
    return performCommandAs(euGrantAccessPostCommand, actor);
  }

  @RequiredArgsConstructor
  public static class GrantEuAccessPermissionBuilder {

    private final EuAccessCode accessCode;

    public GrantEuAccessPermission forCountryOf(EuPharmacyActor euPharmacy) {
      return forCountry(euPharmacy.getCountryCode());
    }

    public GrantEuAccessPermission forCountry(IsoCountryCode countryCode) {
      return new GrantEuAccessPermission(new EuGrantAccessPostCommand(accessCode, countryCode));
    }

    public GrantEuAccessPermission withUncheckedAC(EuAccessPermission euAccessPermission) {
      return new GrantEuAccessPermission(new EuGrantAccessPostCommand(euAccessPermission));
    }
  }
}
