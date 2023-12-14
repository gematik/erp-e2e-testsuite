#
# Copyright 2023 gematik GmbH
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
# language: de

@PRODUKT:eRp_FD
@Workflow:200
@Versicherung:PKV
@AFO-ID:A_18822
@AFO-ID:A_18827
@AFO-ID:A_18502
@AFO-ID:A_18503
Funktionalität: PKV-Rezepte verschreiben
  Für eine PKV-Versicherte ein E-Rezept verschreiben

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und der PKV Versicherte Günther Angermänn hat Zugriff auf seine eGK
    Und die GKV Versicherte Leonie Hütter hat Zugriff auf ihre eGK

  @TCID:ERP_EE_WF200_02
  @TESTFALL:positiv
  @Hauptdarsteller:Arztpraxis
  Szenario: PKV E-Rezept verschreiben und dem Versicherten Anzeigen
  Die Ärztin verschreibt dem PKV-Versicherten Günther Angermänn  ein E-Rezept. Günther Angermänn soll dieses E-Rezept
  im FdV angezeigt werden

    Wenn die Ärztin Dr. Schraßer dem Versicherten Günther Angermänn folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Dann wird dem Versicherten Günther Angermänn das neue E-Rezept angezeigt


  @TCID:ERP_EE_WF200_03
  @AFO-ID:A_22347-01
  @TESTFALL:negativ
  @Hauptdarsteller:Fachdienst
  Szenario: PKV E-Rezept an GKV-Versicherten
  Eine GKV-Versicherte bekommt irrtümlich ein PKV-Rezept verschrieben. Der Fachdienst muss das ablehnen:
  Der E-Rezept-Fachdienst MUSS beim Zugriff auf einen Task des Flowtype 200 mittels HTTP-POST-Operation über
  /Task/<id>/$activate prüfen, ob Coverage.type.coding.code mit dem Wert "PKV" belegt ist
  und im Fehlerfall die Operation mit Http-Fehlercode 400 abbrechen, um sicherzustellen, dass dieser Workflow
  nur für E-Rezepte für PKV-Versicherte genutzt wird.


    Wenn die Versicherte Leonie Hütter die Versicherungsart GKV aufweist
    Dann darf die Ärztin Dr. Schraßer der Versicherten Leonie Hütter das folgende E-Rezept nicht ausstellen:
      | Workflow |
      | 200      |