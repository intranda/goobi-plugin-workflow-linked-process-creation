---
title: Vorgangsbeziehungen anlegen
identifier: intranda_workflow_linkedprocesscreation
description: Dieses Workflow Plugin erlaubt es, mehrere in einer Beziehung zueinander stehende Vorgänge in einer einzelnen Anlegemaske zu erzeugen.
published: true
---
## Einführung
Mit diesem Workflow-Plugin ist es möglich, mehrere in einer Beziehung zueinander stehende Vorgänge in einer einzelnen Anlegemaske zu erzeugen.

Die Anlegemasken sind dabei komplett konfigurierbar und jedes Feld kann einem Metadatum in einem Prozess zugeschrieben werden. Außerdem werden die Beziehungen zwischen den Prozessen konfiguriert.


## Installation
Zur Installation des Plugins müssen folgende drei Dateien installiert werden:

```bash
/opt/digiverso/goobi/plugins/workflow/plugin-intranda-workflow-linkedprocesscreation-base.jar
/opt/digiverso/goobi/plugins/GUI/plugin-intranda-workflow-linkedprocesscreation-gui.jar
/opt/digiverso/goobi/static_assets/plugins/intranda_workflow_linkedprocesscreation/js/app.js
```

Um zu konfigurieren, wie sich das Plugin verhalten soll, können verschiedene Werte in der Konfigurationsdatei angepasst werden. Die Konfigurationsdatei befindet sich üblicherweise hier:

```bash
/opt/digiverso/goobi/config/plugin_intranda_workflow_linkedprocesscreation.xml
```


## Überblick und Funktionsweise
Für eine Nutzung dieses Plugins muss der Nutzer über die korrekte Rollenberechtigung verfügen.

Das Plugin kann im Menü `Workflow` vorgefunden werden. Nachdem die Oberfläche betreten und geladen wurde, kann im unteren linken Bereich zwischen den zur Verfügung stehenden Vorlagen gewechselt werden.

Um Vorgänge anzulegen, sollten durch den Nutzer alle benötigten Felder ausgefüllt werden. Anschließend werden durch einen Klick auf `Vorgänge anlegen` die konfigurierten Vorgänge samt der hinterlegten Metadaten angelegt.


## Konfiguration
Der Inhalt der Konfigurationsdatei sieht wie folgt aus:

```xml
<?xml version="1.0"?>
<config>
    <!--
    Dies ist eine beispielhafte Konfigurationsdatei für die Konfiguration verschiedener Editor-Masken.
    Jede Maske besteht aus zwei Spalten mit mehreren Boxen. In jeder Box können jeweils
    mehrere Felder definiert werden. Ein Feld hat dabei stets einen Feldtyp (z.B. Textbox, Selectbox
    usw.). Ausserdem hat ein Feld eine Bezeichnung und eine Zugehörigkeit zu einem Vorgang.

    Da die Konfiguration beliebige Konstellationen erlaubt, können auf diese Weise aktuelle und künftige
    Erfassungsmasken beliebig zusammengestellt werden und so ein Anlegen verschiedener Vorgänge mit
    Beziehungen untereinander aus einer einzelnen Erfassungsmaske erlauben.
    -->

    <type name="Denkmalakte">
        <process id="1" template="Denkmal"/>
        <process id="2" template="Akte"/>

        <!-- Schreibe das Metadatenfeld "internalNumber" aus Prozess 1
             in das Feld "descriptionOf" in Prozess 2 -->
    <relation
        sourceProcessId="1"
        targetProcessId="2"
        sourceMetadataType="internalNumber"
        targetMetadataType="descriptionOf"/>

        <!-- Spalte 1 -->
        <column>
            <box name="Denkmal">
                <!-- Einfaches Dropdown: Ort -->
                <field type="DROPDOWN" defaultDisplay="true">
                    <metadatatype>place</metadatatype>
                    <name>Ort</name>
                    <!-- die Werte für die Orte sollen aus einem wartbaren Vokabular kommen,
                    dessen Inhalte direkt in Goobi gepflegt werden können -->
                    <sourceVocabulary>places</sourceVocabulary>
                    <processId>1</processId>
                </field>
                <!-- Input Feld: Strasse -->
                <field type="INPUT">
                    <metadatatype>street</metadatatype>
                    <name>Strasse</name>
                    <processId>1</processId>
                </field>
                <!-- Input Feld: Interne Nummer -->
                <field type="INPUT">
                    <metadatatype>internalNumber</metadatatype>
                    <name>Interne Nummer</name>
                    <!-- Dieses Feld wird als Vorgangstitel für Prozess 1 benutzt -->
                    <processTitle>true</processTitle>
                    <processId>1</processId>
                </field>

            </box>

            <box name="Digitalisat">
                <!-- Input Feld: DOI/URI -->
                <field type="INPUT" defaultDisplay="true">
                    <metadatatype>doi</metadatatype>
                    <name>DOI/URI</name>
                    <processId>2</processId>
                </field>
                <!-- Einfaches Dropdown: Datenhaltende Stelle -->
                <field type="DROPDOWN">
                    <metadatatype>dataSource</metadatatype>
                    <name>Datenhaltende Stelle</name>
                    <!-- die Werte für die datenhaltenden Stellen sollen aus einem wartbaren Vokabular kommen,
                    dessen Inhalte direkt in Goobi gepflegt werden können -->
                    <sourceVocabulary>dataSources</sourceVocabulary>
                    <processId>1</processId>
                </field>

            </box>
        </column>

        <!-- Spalte 2 -->
        <column>
            <box name="Akte">
                <!-- Input Feld: Titel -->
                <field type="INPUT" defaultDisplay="true">
                    <metadatatype>title</metadatatype>
                    <name>Titel</name>
                    <processId>2</processId>
                </field>
                <!-- Text-Area: Beschreibung -->
                <field type="TEXTAREA" defaultDisplay="true">
                    <metadatatype>description</metadatatype>
                    <name>Beschreibung</name>
                    <processId>2</processId>
                </field>
                <!-- Input Feld: UUID -->
                <field type="UUID" defaultDisplay="true">
                    <metadatatype>uuid</metadatatype>
                    <name>UUID</name>
                    <!-- Dieses Feld wird als Vorgangstitel für für Prozess 2 benutzt -->
                    <processTitle>true</processTitle>
                    <processId>2</processId>
                </field>
            </box>
        </column>
    </type>

    <!-- Es können mehrere Vorlagen konfiguriert werden -->
    <type>
      ...
    </type>
</config>
```
