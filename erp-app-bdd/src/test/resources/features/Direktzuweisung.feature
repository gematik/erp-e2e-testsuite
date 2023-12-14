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

@iosNightly
@Funktionalität:Direktzuweisung
Funktionalität: E-Rezepte als Direktzuweisung
  Die E-Rezept-Anwendungsfälle für Direktzuweisung für GKV-Versicherte (Workflow 169) und PKV-Versicherte (Workflow 209)

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B

  @Versicherung:GKV
  Szenario: Anzeigen von direkt zugewiesenen E-Rezepten für GKV-Versicherte
    Der Arzt verschreibt der GKV-Versicherten ein Medikament als Direktzuweisung.
    Dieses E-Rezept soll im FdV angezeigt werden.

    Angenommen die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Wenn die Ärztin Dr. Schraßer der Versicherten Alice ein Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist
    Und der Versicherten Alice das letzte E-Rezept in der App angezeigt wird
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensiert
    Dann hat die Apotheke genau eine Quittung vorliegen
    # TODO: check prescription archive as Alice

  @Versicherung:PKV
  Szenario: Anzeigen von direkt zugewiesenen E-Rezepten für PKV-Versicherte
    Der Arzt verschreibt der PKV-Versicherten ein Medikament als Direktzuweisung.
    Dieses E-Rezept soll im FdV angezeigt werden.

    Angenommen die PKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Wenn die Ärztin Dr. Schraßer der Versicherten Alice ein Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist
    Und der Versicherten Alice das letzte E-Rezept in der App angezeigt wird
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensiert
    Dann hat die Apotheke genau eine Quittung vorliegen
    # TODO: check prescription archive as Alice