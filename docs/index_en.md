---
title: Create process relationships
identifier: intranda_workflow_linkedprocesscreation
description: This workflow plugin allows you to create several in a relationship with each other to generate standing processes in a single creation mask.
published: true
keywords:
    - Goobi workflow
    - Plugin
    - Workflow Plugin
---
## Introduction
With this workflow plugin it is possible to create several related processes in a single creation mask.

The creation mask is completely configurable and each field can be assigned to a metadata in a process. Furthermore, the relationships between the processes are configured.


## Installation
To install the plugin, the following three files must be installed:

```bash
/opt/digiverso/goobi/plugins/workflow/plugin-intranda-workflow-linkedprocesscreation-base.jar
/opt/digiverso/goobi/plugins/GUI/plugin-intranda-workflow-linkedprocesscreation-gui.jar
/opt/digiverso/goobi/static_assets/plugins/intranda_workflow_linkedprocesscreation/js/app.js
```

To configure how the plugin should behave, various values can be adjusted in the configuration file. The configuration file is usually located here:

```bash
/opt/digiverso/goobi/config/plugin_intranda_workflow_linkedprocesscreation.xml
```


## Overview and functionality
To use this plugin, the user must have the correct role authorisation.

The plugin can be found in the `Workflow` menu. After entering and loading the interface, you can switch between the available templates in the lower left area.

To create processes, the user should fill in all required fields. Then, by clicking on `Create processes`, the configured processes are created together with the stored metadata.


## Configuration
The contents of this configuration file are as follows:

```xml
<?xml version="1.0"?>
<config>
    <!--
  This is an example configuration file for the configuration of various editor masks.
    Each mask consists of two columns with several boxes. In each box
    several fields can be defined. A field always has a field type (e.g. text box, select box
    etc.). In addition, a field has a name and an association with an activity.

    Since the configuration allows any constellation, current and future
    data entry masks can be combined as required and thus allow the creation of different processes with
    Allow relationships with each other from a single entry mask.
    -->

    <type name="Heritage file">
        <process id="1" template="Monument"/>
        <process id="2" template="File"/>

        <!-- Write the metadata field "internalNumber" from process 1
             into the field "descriptionOf" in process 2 -->
    <relation
        sourceProcessId="1"
        targetProcessId="2"
        sourceMetadataType="internalNumber"
        targetMetadataType="descriptionOf"/>

        <!-- Spalte 1 -->
        <column>
            <box name="Monument">
                <!-- Simple dropdown: Place -->
                <field type="DROPDOWN" defaultDisplay="true">
                    <metadatatype>place</metadatatype>
                    <name>Place</name>
                    <!-- the values for the places should come from a maintainable vocabulary
                    whose content can be maintained directly in Goobi -->
                    <sourceVocabulary>places</sourceVocabulary>
                    <processId>1</processId>
                </field>
                <!-- Input field: Street -->
                <field type="INPUT">
                    <metadatatype>street</metadatatype>
                    <name>Street</name>
                    <processId>1</processId>
                </field>
                <!-- Input field: Internal number -->
                <field type="INPUT">
                    <metadatatype>internalNumber</metadatatype>
                    <name>Interne Nummer</name>
                    <!-- This field is used as process title for process 1 -->
                    <processTitle>true</processTitle>
                    <processId>1</processId>
                </field>

            </box>

            <box name="Digitised version">
                <!-- Input Field: DOI/URI -->
                <field type="INPUT" defaultDisplay="true">
                    <metadatatype>doi</metadatatype>
                    <name>DOI/URI</name>
                    <processId>2</processId>
                </field>
                <!-- Simple dropdown: Data holding unit -->
                <field type="DROPDOWN">
                    <metadatatype>dataSource</metadatatype>
                    <name>Data holding unit</name>
                    <!-- the values for the data holding authorities should come from a maintainable vocabulary
                    whose content can be maintained directly in Goobi -->
                    <sourceVocabulary>dataSources</sourceVocabulary>
                    <processId>1</processId>
                </field>

            </box>
        </column>

        <!-- Spalte 2 -->
        <column>
            <box name="File">
                <!-- Input Field: Title -->
                <field type="INPUT" defaultDisplay="true">
                    <metadatatype>title</metadatatype>
                    <name>Title</name>
                    <processId>2</processId>
                </field>
                <!-- Text-Area: Description -->
                <field type="TEXTAREA" defaultDisplay="true">
                    <metadatatype>description</metadatatype>
                    <name>Description</name>
                    <processId>2</processId>
                </field>
                <!-- Input Feld: UUID -->
                <field type="UUID" defaultDisplay="true">
                    <metadatatype>uuid</metadatatype>
                    <name>UUID</name>
                    <!-- This field is used as process title for process 2 -->
                    <processTitle>true</processTitle>
                    <processId>2</processId>
                </field>
            </box>
        </column>
    </type>

    <!-- Multiple templates can be configured -->
    <type>
      ...
    </type>
</config>
```