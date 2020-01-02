[![Build Status](https://travis-ci.org/Public-Health-Bioinformatics/irida-plugin-resistance-screen.svg?branch=master)](https://travis-ci.org/Public-Health-Bioinformatics/irida-plugin-resistance-screen)
[![codecov](https://codecov.io/gh/Public-Health-Bioinformatics/irida-plugin-resistance-screen/branch/master/graph/badge.svg)](https://codecov.io/gh/Public-Health-Bioinformatics/irida-plugin-resistance-screen)
[![Current Release Version](https://img.shields.io/github/release/Public-Health-Bioinformatics/irida-plugin-resistance-screen.svg)](https://github.com/Public-Health-Bioinformatics/irida-plugin-resistance-screen/releases)

# IRIDA Resistance-Screen Pipeline Plugin

![galaxy-workflow-diagram.png][]

This project contains a pipeline implemented as a plugin for the [IRIDA][] bioinformatics analysis system. 
This can be used to detect the presence of specific resistance genes in a sample.

# Table of Contents

   * [IRIDA Resistance Screen Pipeline Plugin](#irida-resistance-screen-pipeline-plugin)
   * [Installation](#installation)
      * [Installing Galaxy Dependencies](#installing-galaxy-dependencies)
      * [Installing to IRIDA](#installing-to-irida)
      * [Setting up your abricate report screening file(s)](#setting-up-your-abricate-report-screening-files)
   * [Usage](#usage)
      * [Analysis Results](#analysis-results)
      * [Metadata Table](#metadata-table)
   * [Building](#building)
      * [Installing IRIDA to local Maven repository](#installing-irida-to-local-maven-repository)
      * [Building the plugin](#building-the-plugin)
   * [Dependencies](#dependencies)

# Installation

## Installing Galaxy Dependencies

In order to use this pipeline, you will also have to install the following Galaxy tools and data 
managers within your Galaxy instance. These can be found at:

| Name                               | Version         | Owner                          | Metadata Revision | Galaxy Toolshed Link                                                                                                                              |
|------------------------------------|-----------------|------------------------------- |-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| shovill                            | `1.0.4`         | `iuc`                          | 3 (2018-11-13)    | [shovill-3:865119fcb694](https://toolshed.g2.bx.psu.edu/view/iuc/shovill/865119fcb694)                                                            |
| quast                              | `5.0.2`         | `iuc`                          | 5 (2018-12-04)    | [quast-5:81df4950d65b](https://toolshed.g2.bx.psu.edu/view/iuc/quast/81df4950d65b)                                                                |
| abricate                           | `0.9.8`         | `iuc`                          | 7 (2019-10-29)    | [abricate-7:4efdca267d51](https://toolshed.g2.bx.psu.edu/view/iuc/abricate/4efdca267d51)                                                          |
| screen_abricate_report             | `0.1.0`         | `public-health-bioinformatics` | 0 (2019-10-31)    | [screen_abricate_report-0:b2d56a44a872](https://toolshed.g2.bx.psu.edu/view/public-health-bioinformatics/screen_abricate_report/b2d56a44a872)     |
| data_manager_manual                | `0.0.2`         | `iuc`                          | 5 (2019-10-21)    | [data_manager_manual-5:744f607fac50](https://toolshed.g2.bx.psu.edu/view/iuc/data_manager_manual/744f607fac50)                                    |

## Installing to IRIDA

Please download the provided `irida-plugin-resistance-screen-[version].jar` from the [releases][] page and copy to your 
`/etc/irida/plugins` directory.  Now you may start IRIDA and you should see the pipeline appear in your list of pipelines.

*Note:* This plugin requires you to be running IRIDA version >= `19.01`. Please see the [IRIDA][] documentation for more details.

## Setting up your abricate report screening file(s)

Abricate report screening files have a simple tabular format, and can be created with Excel, another spreadsheet application,
or a plaintext editor. They consist of two columns, with headings `gene_name` and `regex`. All fields should be tab-delimited.

```
gene_name    regex
KPC          KPC
OXA-48       OXA\-48
NDM          NDM
```

## Preparing the 'abricate_report_screening_files' Tool Data Table in Galaxy

This workflow requires that the abricate report screening files described above are made available via a 
[Galaxy Tool Data Table](https://galaxyproject.org/admin/tools/data-tables/) called `abricate_report_screening_files`.
We recommend that you use the [`data_manager_manual`](https://github.com/galaxyproject/tools-iuc/tree/master/data_managers/data_manager_manual) 
tool to manage that data table.

# Usage

The plugin should now show up in the **Analyses > Pipelines** section of IRIDA.

![plugin-pipeline.png][]
![pipeline-parameters.png][]

## Analysis Results

You should be able to run a pipeline with this plugin and get analysis results. The results include a full `abricate` 
report, and a screened `abricate` report that includes only your genes of interest.

![plugin-results-1.png][]
![plugin-results-2.png][]
![plugin-results-3.png][]

## Metadata Table

And, you should be able to save and view these results in the IRIDA metadata table. The following fields are written to
the IRIDA 'Line List':

| Field Name                                 | Description                                               |
|--------------------------------------------|-----------------------------------------------------------|
| resistance-screen/<GENE_NAME>/detected     | Whether or not `GENE_NAME` was detected (True/False)      |

**Note**: If your abricate report screening file contains many genes, this will result in many columns 

![plugin-metadata.png][]

# Building

Building and packaging this code is accomplished using [Apache Maven][maven]. However, you will first need to install [IRIDA][] to your local Maven repository. The version of IRIDA you install will have to correspond to the version found in the `irida.version.compiletime` property in the [pom.xml][] file of this project. Right now, this is IRIDA version `19.01.3`.

## Installing IRIDA to local Maven repository

To install IRIDA to your local Maven repository please do the following:

1. Clone the IRIDA project

```bash
git clone https://github.com/phac-nml/irida.git
cd irida
```

2. Checkout appropriate version of IRIDA

```bash
git checkout 19.01.3
```

3. Install IRIDA to local repository

```bash
mvn clean install -DskipTests
```

## Building the plugin

Once you've installed IRIDA as a dependency, you can proceed to building this plugin. Please run the following commands:

```bash
cd irida-plugin-resistance-screen

mvn clean package
```

Once complete, you should end up with a file `target/irida-plugin-resistance-screen-0.1.0-SNAPSHOT.jar` which can be installed as a plugin to IRIDA.

# Dependencies

The following dependencies are required in order to make use of this plugin.

* [IRIDA][] >= 0.23.0
* [Java][] >= 1.8 and [Maven][maven] (for building)

[maven]: https://maven.apache.org/
[IRIDA]: http://irida.ca/
[Galaxy]: https://galaxyproject.org/
[Java]: https://www.java.com/
[irida-pipeline]: https://irida.corefacility.ca/documentation/developer/tools/pipelines/
[irida-pipeline-galaxy]: https://irida.corefacility.ca/documentation/developer/tools/pipelines/#galaxy-workflow-development
[irida-wf-ga2xml]: https://github.com/phac-nml/irida-wf-ga2xml
[pom.xml]: pom.xml
[workflows-dir]: src/main/resources/workflows
[workflow-structure]: src/main/resources/workflows/0.1.0/irida_workflow_structure.ga
[example-plugin-java]: src/main/java/ca/corefacility/bioinformatics/irida/plugins/ExamplePlugin.java
[irida-plugin-java]: https://github.com/phac-nml/irida/tree/development/src/main/java/ca/corefacility/bioinformatics/irida/plugins/IridaPlugin.java
[irida-updater]: src/main/java/ca/corefacility/bioinformatics/irida/plugins/ExamplePluginUpdater.java
[irida-setup]: https://irida.corefacility.ca/documentation/administrator/index.html
[properties]: https://en.wikipedia.org/wiki/.properties
[messages]: src/main/resources/workflows/0.1.0/messages_en.properties
[maven-min-pom]: https://maven.apache.org/guides/introduction/introduction-to-the-pom.html#Minimal_POM
[pf4j-start]: https://pf4j.org/doc/getting-started.html
[plugin-results-1.png]: doc/images/plugin-results-1.png
[plugin-results-2.png]: doc/images/plugin-results-2.png
[plugin-results-3.png]: doc/images/plugin-results-3.png
[plugin-pipeline.png]: doc/images/plugin-pipeline.png
[plugin-metadata.png]: doc/images/plugin-metadata.png
[pipeline-parameters.png]: doc/images/pipeline-parameters.png
[example-plugin-save-results.png]: doc/images/example-plugin-save-results.png
[galaxy-workflow-diagram.png]: doc/images/galaxy-workflow-diagram.png
