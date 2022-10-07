#
# Copyright (c) 2022 gematik GmbH
# 
# Licensed under the Apache License, Version 2.0 (the License);
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an 'AS IS' BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# language: de

@Funktionalität=Direktzuweisung
@Workflow=209
@Anwendungsfall=A_18502
@Anwendungsfall=A_18503
@Versicherung=PKV
@Impl=done
Funktionalität: Direktzuweisung für PKV-Rezepte
  E-Rezept für eine PKV-Versicherte als Direktzuweisung (WF 209)

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und der PKV Versicherte Günther Angermänn hat Zugriff auf seine digitale Identität
    Wenn die Ärztin Dr. Schraßer dem Versicherten Günther Angermänn folgendes Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist:
      | Name              |
      | Zytostatika_WF209 |

  @TCID=ERP_EE_WF209_01
  @Path=happy
  @Afo=A_21360-01
  Szenario: E-Rezept als Direktzuweisung verschreiben, Anzeige im FdV (WF 209)
  Die Ärztin verschreibt dem PKV-Versicherten Günther Angermänn  ein E-Rezept als Dierektzuweisung. Günther Angermänn soll dieses E-Rezept
  im FdV angezeigt werden

    Dann wird dem Versicherten Günther Angermänn das neue E-Rezept im FdV angezeigt ohne AccessCode

  @TCID=ERP_EE_WF209_02
  @Path=happy
  Szenario: Direktzuweisung an eine Apotheke durch den Arzt (WF 209)
  Der Arzt verschreibt der GKV-Versicherten ein Medikament als Direktzuweisung und übermittelt es der Apotheke.
  Die Apotheke kann es erfolgreich dispensieren.

    Wenn die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensiert
    Dann hat die Apotheke genau eine Quittung vorliegen

  @TCID=ERP_EE_WF209_03
  @Impl=open
  @Path=happy
  Szenario: Bereitstellen der Abrechnungsinformationen durch die Apotheke (WF 209)
  Nach der erfolgreichen Dispensierung kann die Apotheke die Abrechnungsinformationen zum E-Rezept bereitstellen.

    Angenommen der Versicherte Günther Angermänn hat seine Einwilligung zum Speichern der Abrechnungsinformationen erteilt
    Wenn die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensiert
    Und die Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt
    Dann kann der Versicherte Günther Angermänn für das letzte E-Rezept die Abrechnungsinformationen im FdV abrufen


  @TCID=ERP_EE_WF209_04
  @Afo=A_22209
  @Path=bad
  @Impl=open
  Szenario: Keine Abrechnungsinformationen bereitstellen ohne Einwilligung (WF 209)
  Nach der erfolgreichen Dispensierung darf die Apotheke die Abrechnungsinformationen zum E-Rezept nicht bereitstellen, weil keine Einwilligung des Versicherten vorliegt.

    Angenommen der Versicherte Günther Angermänn hat seine Einwilligung zum Speichern der Abrechnungsinformationen widerrufen
    Wenn die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensiert
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept keinen PKV-Abrechnungsdatensatz beim Fachdienst hinterlegen, weil keine Einwilligung vorliegt
    Dann kann der Versicherte Günther Angermänn für das letzte E-Rezept keine Abrechnungsinformationen im FdV abrufen

  @TCID=ERP_EE_WF209_05
  @Afo=A_22102-01
  @Path=bad
  Szenario: Löschen nicht möglich während der Dispensierung
  Der Versicherte kann das E-Rezept nicht löschen während die Apotheke das Rezept dispensiert

    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann wird der Versicherten Günther Angermänn das neue E-Rezept im FdV angezeigt
    Und der Versicherte Günther Angermänn kann sein letztes E-Rezept nicht löschen

  @TCID=ERP_EE_WF209_06
  @Afo=A_22102-01
  @Path=bad
  Szenario: Löschen nicht möglich bis nach der Dispensierung
  Der Versicherte kann E-Rezepte mit Direktzuweisung nicht löschen bis diese durch
  eine Apotheke dispensiert wurden

    Dann wird dem Versicherten Günther Angermänn das neue E-Rezept im FdV angezeigt
    Und der Versicherte Günther Angermänn kann sein letztes E-Rezept nicht löschen

  @TCID=ERP_EE_WF209_07
  @Afo=A_22102-01
  @Path=happy
  Szenario: Löschen möglich nach der Dispensierung
  Der Versicherte kann das E-Rezept erst löschen nachdem die Apotheke das Rezept dispensiert hat

    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensiert
    Und der Versicherte Günther Angermänn sein letztes E-Rezept löscht
    Dann wird dem Versicherten sein letztes gelöschte E-Rezept nicht mehr im FdV angezeigt



  @TCID=ERP_EE_WF209_08
  @Afo=A_22347-01
  @Path=bad
  @Impl=open
  Szenario: PKV E-Rezept als Direktzuweisung an GKV-Versicherten nicht möglich
  Eine GKV-Versicherte bekommt irrtümlich ein PKV-Rezept als Direktzuweisung verschrieben (WF 209). Der Fachdienst muss das ablehnen:
  Der E-Rezept-Fachdienst MUSS beim Zugriff auf einen Task des Flowtype 200 mittels HTTP-POST-Operation über /Task/<id>/$activate prüfen, ob Coverage.type.coding.code mit dem Wert "PKV" belegt ist
  und im Fehlerfall die Operation mit Http-Fehlercode 400 abbrechen, um sicherzustellen, dass dieser Workflow nur für E-Rezepte für PKV-Versicherte genutzt wird.


    Wenn die Versicherte Leonie Hütter die Versicherungsart GKV aufweist
    Dann darf die Ärztin Dr. Schraßer der Versicherten Leonie Hütter das folgende E-Rezept nicht ausstellen:
      | Name              | Workflow |
      | Zytostatika_WF209 | 209      |
