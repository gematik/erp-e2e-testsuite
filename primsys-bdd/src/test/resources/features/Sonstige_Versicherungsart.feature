#
# Copyright 2024 gematik GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# *******
# For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
# *******
#
# language: de

@PRODUKT:eRp_FD
@Workflow:160
@Versicherung:Sonstige
@STATUS:InBearbeitung
Funktionalität: Sonstige Versicherungsarten

  @TCID:ERP_EE_SONSTIGE_VERSICHERUNGSART_01
  @TESTFALL:negativ
  @AFO-ID:A_22222
  @Hauptdarsteller:Fachdienst
  Szenariogrundriss: Sonstige Versicherungsarten sind nicht zulässig
  Der E-Rezept Fachdienst muss das Ausstellen von E-Rezepten unterbinden, wenn ein unzulässiger Kostenträger
  gemäß der Anforderung A_22222 im QES-Datensatz hinterlegt ist.

    Angenommen die Ärztin Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    # TODO: implement this test scenario as a negative test case in produkt-testsuite
    # has @STATUS:InBearbeitung because of
    # 1. this approach was never properly working because it is impossible to generate a fhir patient with insurance type SOZ etc.
    # 2. as the description states, the <Versicherungsart> must be placed in the fhir coverage resource (which is not possible with the current implementation)
    Und der <Versicherungsart> Versicherte Fridolin Straßer hat Zugriff auf seine eGK

    Wenn der Versicherte Fridolin Straßer die Versicherungsart <Versicherungsart> aufweist
    Dann darf der Arzt Adelheid Ulmenwald dem Versicherten Fridolin Straßer das folgende E-Rezept nicht ausstellen:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |

    Beispiele:
      | Versicherungsart |
      | SOZ              |
      | GPV              |
      | PPV              |
      | BEI              |