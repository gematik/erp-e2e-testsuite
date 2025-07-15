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

package de.gematik.test.erezept.app.questions;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.Receipt;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DateOfIssueTest {
  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void isIssuedTodayTest() {
    val useMockAppAbility = mock(UseIOSApp.class);
    val dd = LocalDate.now();
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    val formattedDate = dd.format(formatter);
    when(useMockAppAbility.getText(Receipt.RECEIPT_DATE_OF_ISSUE_LABEL)).thenReturn(formattedDate);
    val actor = OnStage.theActor("Alice").can(useMockAppAbility);
    assertTrue(actor.asksFor(IsIssued.today()));
  }

  @Test
  void isIssuedOnTest() {
    val useMockAppAbility = mock(UseIOSApp.class);
    when(useMockAppAbility.getText(Receipt.RECEIPT_DATE_OF_ISSUE_LABEL)).thenReturn("01.01.2020");
    val actor = OnStage.theActor("Alice").can(useMockAppAbility);
    assertFalse(actor.asksFor(IsIssued.on(LocalDate.now())));
  }
}
