# OrthoMCLService
A REST service that provides record pages, searching and analysis backend support for [OrthoMCL.org].  It is an extension of [EbrcWebSvcCommon](https://github.com/VEuPathDB/EbrcWebSvcCommon), which is, in turn, an extension of the REST services provided by the [WDK](https://github.com/VEuPathDB/WDK).

This extension provides endpoints beyond those available in [EbrcWebSvcCommon](https://github.com/VEuPathDB/EbrcWebSvcCommon):
  + a [DataSummaryService](Service/src/main/java/org/orthomcl/service/services/DataSummaryService.java) provides data that summarizes the contents of the OrthoMCL database
  + a [GroupLayoutService](Service/src/main/java/org/orthomcl/service/services/GroupLayoutService.java) provides data that drives a visualization of the relatedness of proteins in an OrthoMCL group.
  + a [NewickProteinTreeService](Service/src/main/java/org/orthomcl/service/services/NewickProteinTreeService.java) provides data that drives a tree visualization of the proteins in an OrthoMCL group.

## Dependencies

   + ant
   + Java 11+
   + External dependencies: see [pom.xml](pom.xml)
   + environment variables for GUS_HOME and PROJECT_HOME
   + Internal Dependencies: see [build.xml](build.xml)

## Installation instructions.

   + bld OrthoMCLWdkService
