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
@Funktionalität:Abrechnungsinformationen
@Versicherung:GKV
Funktionalität: Keine Abrechnungsinformationen für GKV-Rezepte
  Abrechnungsinformationen sind für PKV-Versicherter vorbehalten und dürfen nicht an GKV-Versicherte ausgestellt werden


  @TCID:ERP_EE_GKV_ABRECHNUNGSINFO_01
    @TESTFALL:negativ
    @AFO-ID:A_22731
    @Hauptdarsteller:Apotheke
  Szenariogrundriss: Keine PKV-Abrechnungsinformationen für GKV Workflows
  Keine PKV-Abrechnungsinformationen bereitstellen bei Workflow 169 und 160

    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die GKV Versicherte Hanna Bäcker hat Zugriff auf ihre eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Hanna Bäcker folgendes apothekenpflichtiges Medikament verschreibt:
      | Workflow |
      | <WF>     |
    Und die Versicherte Hanna Bäcker sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Hanna Bäcker dispensiert
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept keine PKV-Abrechnungsinformationen bereitstellen, weil es kein PKV-Rezept ist

    Beispiele:
      | WF  |
      | 160 |
      | 169 |