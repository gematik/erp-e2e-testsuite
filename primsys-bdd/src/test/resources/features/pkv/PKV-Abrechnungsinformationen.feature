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
@Workflow:200
@Versicherung:PKV
@AFO-ID:A_18822
@AFO-ID:A_18827
Funktionalität: Abrechnungsinformationen für PKV-Rezepte
  Umfasst Testfälle zu den Anwendungsfällen:
  - PKV-Abrechnungsinformationen durch den Abgebenden bereitstellen
  - PKV-Abrechnungsinformationen durch den Abgebenden abrufen
  - PKV-Abgabedatensatz ändern
  - Einwilligung durch den Versicherten erteilen
  - Einwilligung durch den Versicherten widerrufen
  - Einwilligung durch den Versicherten einsehen
  - Abrufen der PKV-Abrechnungsinformationen durch den Versicherten
  - Markieren der Abrechnungsinformation durch den Versicherten
  - Löschen der Abrechnungsinformation durch den Versicherten
  - Berechtigen der Apotheke zum Ändern des PKV-Abgabedatensatzes


  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und der PKV Versicherte Günther Angermänn hat Zugriff auf seine digitale Identität
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und der Versicherte Günther Angermänn hat seine Einwilligung zum Speichern der PKV-Abrechnungsinformationen erteilt
    Und die Ärztin Dr. Schraßer dem Versicherten Günther Angermänn folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | Workflow |
      | Schmerzmittel | 200      |
    Und der Versicherte Günther Angermänn sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Günther Angermänn dispensiert

  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_01
  @TESTFALL:positiv
  @AFO-ID:A_22118
  @AF-ID:AF_10082
  @AF-ID:AF_10087
  @Hauptdarsteller:Apotheke
  Szenario: Bereitstellen der PKV-Abrechnungsinformationen durch die Apotheke

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Dann hat der Versicherte Günther Angermänn die PKV-Abrechnungsinformationen für das letzte dispensierte Medikament beim Fachdienst vorliegen

  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_02
  @TESTFALL:negativ
  @AFO-ID:A_22133
  @Hauptdarsteller:Apotheke
  Szenario: Keine PKV-Abrechnungsinformationen bereitstellen, wenn keine Einwilligung erteilt
  Falls für den Versicherten keine Einwilligung beim Fachdienst zur Speicherung der PKV-Abrechnungsinformationen vorliegt,
  muss der Fachdienst das Hochladen des ChargeItems mit Fehlercode 403 ablehnen

    Wenn der Versicherte Günther Angermänn seine Einwilligung zur Speicherung der PKV-Abrechnungsinformationen widerruft
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept keine PKV-Abrechnungsinformationen bereitstellen, weil keine Einwilligung vorliegt


  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_03
  @TESTFALL:positiv
  @AFO-ID:A_22161
  @AF-ID:AF_10084
  @AF-ID:AF_10086
  @Hauptdarsteller:Versicherter
  Szenario: Einwilligung durch Versicherten erteilen
  Der Versicherte kann im FdV die Einwilligung zum Speichern der PKV-Abrechnungsinformationen beim Fachdienst erteilen. Diese wird ihm im FdV angezeigt

    Wenn der Versicherte Günther Angermänn seine Einwilligung zur Speicherung der PKV-Abrechnungsinformationen erteilt
    Dann kann der Versicherte Günther Angermänn seine Einwilligung abrufen

  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_04
  @TESTFALL:positiv
  @AFO-ID:A_22157
  @AF-ID:AF_10085
  @Hauptdarsteller:Versicherter
  Szenario: PKV-Abrechnungsinformationen löschen, wenn die Einwilligung vom Versicherten widerrufen wird
  Falls der Versicherte nach dem Speichern von PKV-Abrechnungsinformationen seine Einwilligung widerruft,
  werden die vorhandenen PKV-Abrechnungsinformationen beim Fachdienst gelöscht.
  Der E-Rezept-Fachdienst MUSS beim Aufruf der HTTP-Operation DELETE auf den Endpunkt /Consent mit
  ?category=CHARGCONS alle dem Versicherten zugeordneten ChargeItem-Ressourcen (ChargeItem.subject.identifier)
  anhand der KVNR des Versicherten im ACCESS_TOKEN im "Authorization"-Header des HTTP-Requests identifizieren und löschen.

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Und der Versicherte Günther Angermänn für das letzte E-Rezept die PKV-Abrechnungsinformationen abruft
    Und der Versicherte Günther Angermänn seine Einwilligung zur Speicherung der PKV-Abrechnungsinformationen widerruft
    Dann kann der Versicherte Günther Angermänn für das letzte E-Rezept keine PKV-Abrechnungsinformationen abrufen

  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_05
  @TESTFALL:positiv
  @AFO-ID:A_22113
  @AFO-ID:A_22114
  @AF-ID:AF_10090
  @Hauptdarsteller:Versicherter
  Szenario: Löschen der PKV-Abrechnungsinformationen durch den Versicherten
  Der Versicherte kann über das FdV die PKV-Abrechnungsinformationen zu einem Rezept löschen.
  Anschließend können sie nicht mehr angezeigt werden (FC 404)

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Und der Versicherte Günther Angermänn für das letzte E-Rezept die PKV-Abrechnungsinformationen löscht
    Dann kann der Versicherte Günther Angermänn für das letzte E-Rezept keine PKV-Abrechnungsinformationen abrufen, weil sie nicht gefunden werden

  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_06
  @TESTFALL:positiv
  @AF-ID:AF_10083
  @AF-ID:AF_10081
  @Hauptdarsteller:Apotheke
  Szenario: Ändern des PKV-Abgabedatensatzes auf Wunsch des Versicherten, Übertragung des AccessCodes per DMC

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Und der Versicherte Günther Angermänn für das letzte E-Rezept die PKV-Abrechnungsinformationen abruft
    Und der Versicherte Günther Angermänn die Apotheke Am Flughafen via Data Matrix Code zum Ändern des letzten PKV-Abgabedatensatzes berechtigt
    Und die Apotheke Am Flughafen den letzten autorisierten PKV-Abgabedatensatz für das dispensierte E-Rezept ändert
    Dann kann der Versicherte Günther Angermänn für das letzte E-Rezept die geänderte PKV-Abrechnungsinformationen abrufen


  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_07
  @TESTFALL:positiv
  @AF-ID:AF_10083
  @Hauptdarsteller:Apotheke
  Szenario: Ändern des PKV-Abgabedatensatzes auf Wunsch des Versicherten, Übertragung des AccessCodes per Nachricht

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Und der Versicherte Günther Angermänn für das letzte E-Rezept die PKV-Abrechnungsinformationen abruft
    Und der Versicherte Günther Angermänn die Apotheke Am Flughafen per Nachricht zum Ändern der PKV-Abrechnungsinformationen berechtigt
    Und die Apotheke Am Flughafen die letzte Nachricht des Versicherten Günther Angermänn mit dem Änderungswunsch empfängt und beantwortet
    Und die Apotheke Am Flughafen den letzten autorisierten PKV-Abgabedatensatz für das dispensierte E-Rezept ändert
    Dann hat der Versicherte Günther Angermänn eine Antwort auf seinen Änderungswunsch von der Apotheke Am Flughafen erhalten
    Und der Versicherte Günther Angermänn kann für das letzte E-Rezept die geänderte PKV-Abrechnungsinformationen abrufen


  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_08
  @TESTFALL:negativ
  @AFO-ID:A_22616
  @Hauptdarsteller:Apotheke
  Szenario: Ändern des PKV-Abgabedatensatzes nur einmal möglich mit gleichem AccessCode

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Und der Versicherte Günther Angermänn für das letzte E-Rezept die PKV-Abrechnungsinformationen abruft
    Und der Versicherte Günther Angermänn die Apotheke Am Flughafen via Data Matrix Code zum Ändern des letzten PKV-Abgabedatensatzes berechtigt
    Dann kann die Apotheke Am Flughafen den letzten autorisierten PKV-Abgabedatensatz für das dispensierte E-Rezept erstmalig ändern
    Und die Apotheke Am Flughafen kann den letzten autorisierten PKV-Abgabedatensatz für das dispensierte E-Rezept nicht erneut ändern, weil der Datensatz bereits geändert wurde

  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_09
  @TESTFALL:negativ
  @AFO-ID:A_22215
  @Hauptdarsteller:Apotheke
  Szenario: Ändern des PKV-Abgabedatensatzes nicht möglich, weil Einwilligung widerrufen

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Und der Versicherte Günther Angermänn für das letzte E-Rezept die PKV-Abrechnungsinformationen abruft
    Und der Versicherte Günther Angermänn die Apotheke Am Flughafen via Data Matrix Code zum Ändern des letzten PKV-Abgabedatensatzes berechtigt
    Und der Versicherte Günther Angermänn seine Einwilligung zur Speicherung der PKV-Abrechnungsinformationen widerruft
    Dann kann die Apotheke Am Flughafen den letzten autorisierten PKV-Abgabedatensatz für das dispensierte E-Rezept nicht ändern, weil sie kein Recht dazu hat
    Und der Versicherte Günther Angermänn kann für das letzte E-Rezept keine PKV-Abrechnungsinformationen mehr abrufen

  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_10
  @TESTFALL:positiv
  @AF-ID:AF_10089
  @Hauptdarsteller:Versicherter
  Szenariogrundriss: Markieren des PKV-Abgabedatensatzes durch Versicherten

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Und der Versicherte Günther Angermänn die Markierungen für die PKV-Abrechnungsinformationen des letzten E-Rezept setzt:
      | Versicherung   | Beihilfe   | Finanzamt   |
      | <Versicherung> | <Beihilfe> | <Finanzamt> |
    Dann hat der Versicherte Günther Angermänn die PKV-Abrechnungsinformationen des letzten E-Rezepts mit folgenden Markierungen:
      | Versicherung   | Beihilfe   | Finanzamt   |
      | <Versicherung> | <Beihilfe> | <Finanzamt> |

    Beispiele:
      | Versicherung | Beihilfe | Finanzamt |
      | true         | true     | false     |
      | true         | false    | true      |
      | true         | false    | false     |
      | false        | true     | true      |
      | false        | true     | false     |
      | false        | false    | true      |
      | false        | false    | false     |


  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_11
  @TESTFALL:positiv
  @AF-ID:AF_10089
  @Hauptdarsteller:Versicherter
  Szenariogrundriss: Löschen von Markierungen des PKV-Abgabedatensatzes durch Versicherten

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Und der Versicherte Günther Angermänn die Markierungen für die PKV-Abrechnungsinformationen des letzten E-Rezept setzt:
      | Versicherung   | Beihilfe   | Finanzamt   |
      | <Versicherung> | <Beihilfe> | <Finanzamt> |
    Dann hat der Versicherte Günther Angermänn die PKV-Abrechnungsinformationen des letzten E-Rezepts mit folgenden Markierungen:
      | Versicherung   | Beihilfe   | Finanzamt   |
      | <Versicherung> | <Beihilfe> | <Finanzamt> |

    Beispiele:
      | Versicherung | Beihilfe | Finanzamt |
      | true         | true     | true      |
      | true         | true     | false     |
      | true         | false    | true      |
      | true         | false    | false     |
      | false        | true     | true      |
      | false        | true     | false     |
      | false        | false    | true      |
      | false        | false    | false     |


  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_12
  @TESTFALL:positiv
  @Hauptdarsteller:Apotheke
  Szenario: Bereitstellen der PKV-Abrechnungsinformationen durch die Apotheke nach Änderung der Medikation
  Der PKV-Abgabedatensatz muss mit dem HBA signiert werden, wenn die Medikation gegenüber dem ursprünglichem Rezept verändert wurde

    Angenommen der Apotheker Finn-Louis Nullmayr hat Zugriff auf seinen HBA
    Wenn der Apotheker Finn-Louis Nullmayr als Angestellter der Apotheke Am Flughafen für das letzte dispensierte E-Rezept PKV-Abrechnungsinformationen bereitstellt
    Dann hat der Versicherte Günther Angermänn die PKV-Abrechnungsinformationen für das letzte dispensierte Medikament beim Fachdienst vorliegen

  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_13
  @TESTFALL:negativ
  @AFO-ID:A_22131
  @Hauptdarsteller:Apotheke
  Szenario: Bereitstellen der PKV-Abrechnungsinformationen nicht möglich, weil E-Rezept gelöscht wurde

    Wenn der Versicherte Günther Angermänn sein letztes E-Rezept löscht
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept keine PKV-Abrechnungsinformationen bereitstellen, weil der Task nicht mehr existiert

  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_14
  @AFO-ID:A_22132
  @TESTFALL:negativ
  @Hauptdarsteller:Apotheke
  Szenario: Keine PKV-Abrechnungsinformationen bereitstellen bei ungültigem Secret

    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept keine PKV-Abrechnungsinformationen mit dem falschen Secret fgdkjfgd bereitstellen


  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_15
  @TESTFALL:positiv
  @AFO-ID:A_22611-01
  @AFO-ID:A_22128
  @Hauptdarsteller:Apotheke
  Szenario: Abrufen der Abrechnungsinformationen durch eine Apotheke mit AccessCode

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Und der Versicherte Günther Angermänn für das letzte E-Rezept die PKV-Abrechnungsinformationen abruft
    Und der Versicherte Günther Angermänn die Apotheke Am Flughafen via Data Matrix Code zum Ändern des letzten PKV-Abgabedatensatzes berechtigt
    Dann kann die Apotheke Am Flughafen die letzten berechtigten PKV-Abrechnungsinformationen vom Fachdienst abrufen

  @TCID:ERP_EE_WF200_ABRECHNUNGSINFO_16
  @TESTFALL:negativ
  @AFO-ID:A_22611-01
  @Hauptdarsteller:Apotheke
  Szenario: Abrufen der Abrechnungsinformationen durch einen Apotheker mit falschem AccessCode

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Und der Versicherte Günther Angermänn für das letzte E-Rezept die PKV-Abrechnungsinformationen abruft
    Und der Versicherte Günther Angermänn die Apotheke Am Flughafen via Data Matrix Code zum Ändern des letzten PKV-Abgabedatensatzes berechtigt
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept den PKV-Abrechnungsinformationen nicht mit dem falschen AccessCode abc vom Fachdienst abrufen