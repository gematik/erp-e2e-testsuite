#
# Copyright 2025 gematik GmbH
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
@Funktionalität:DiGA
@Workflow:162
@Versicherung:GKV
@AFO-ID:A_19225-02
@AFO-ID:A_25990
@Hauptdarsteller:Therapeut
Funktionalität: DiGA negativ Anwendungsfälle

  Grundlage:
    Angenommen der Kostenträger AOK Bremen hat Zugriff auf seine SMC-B KTR

  @TCID:ERP_EE_WF162_01
  Szenariogrundriss: Psychotherapeuten dürfen keine Apothekenpflichtigen E-Rezepte erstellen
    Angenommen der <VerordnendeRolle> <VerordnendeName> hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und die <Versicherungsart> Versicherte Hanna Bäcker hat Zugriff auf ihre eGK
    Dann kann der <VerordnendeRolle> <VerordnendeName> der Versicherten Hanna Bäcker kein apothekenpflichtiges Rezept verschreiben

    Beispiele:
      | VerordnendeRolle                | VerordnendeName    | Versicherungsart |
      | Psychotherapeut                 | Finn Stauffenberg  | GKV              |
      | Psychologischer Psychotherapeut | Julian Böcher      | GKV              |
      | Kinderpsychotherapeut           | Peter Kleinschmidt | GKV              |
      | Psychotherapeut                 | Finn Stauffenberg  | PKV              |
      | Psychologischer Psychotherapeut | Julian Böcher      | PKV              |
      | Kinderpsychotherapeut           | Peter Kleinschmidt | PKV              |


 @TCID:ERP_EE_WF162_02
 Szenariogrundriss: Psychotherapeuten dürfen keine E-Rezepte Direktzuweisung erstellen
    Angenommen der <VerordnendeRolle> <VerordnendeName> hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
   Und die <Versicherungsart> Versicherte Hanna Bäcker hat Zugriff auf ihre eGK
    Dann kann der <VerordnendeRolle> <VerordnendeName> der Versicherten Hanna Bäcker kein Rezept als Direktzuweisung verschreiben

    Beispiele:
      | VerordnendeRolle                | VerordnendeName    | Versicherungsart |
      | Psychotherapeut                 | Finn Stauffenberg  | GKV              |
      | Psychologischer Psychotherapeut | Julian Böcher      | GKV              |
      | Kinderpsychotherapeut           | Peter Kleinschmidt | GKV              |
      | Psychotherapeut                 | Finn Stauffenberg  | PKV              |
      | Psychologischer Psychotherapeut | Julian Böcher      | PKV              |
      | Kinderpsychotherapeut           | Peter Kleinschmidt | PKV              |


