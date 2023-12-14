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

package de.gematik.test.erezept.app.questions;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.Receipt;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor
public class IsIssued implements Question<Boolean> {
  private final LocalDate expectedDate;

  @Override
  public Boolean answeredBy(Actor actor) {
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    driverAbility.tap(Receipt.GOBACK_TO_DETAILS);
    val dateOfIssue = driverAbility.getText(Receipt.RECEIPT_DATE_OF_ISSUE_LABEL);

    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    val formattedDate = this.expectedDate.format(formatter);
    return dateOfIssue.equals(formattedDate);
  }

  public static IsIssued today() {
    return new IsIssued(LocalDate.now());
  }


  public static IsIssued on(LocalDate date) {
    return new IsIssued(date);
  }
}
