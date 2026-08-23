package com.everrefine.elms.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "com.everrefine.elms",
    importOptions = ImportOption.DoNotIncludeTests.class)
class LayerDependencyTest {

  @ArchTest
  static final ArchRule presentationはinfrastructureに依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..presentation..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..infrastructure..")
          .as("Rule 1: presentation層はinfrastructure層にアクセスしてはならない");

  @ArchTest
  static final ArchRule applicationはinfrastructureに依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..infrastructure..")
          .as("Rule 2: application層はinfrastructure層にアクセスしてはならない");

  @ArchTest
  static final ArchRule applicationはpresentationに依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..presentation..")
          .as("Rule 3: application層はpresentation層にアクセスしてはならない");

  @ArchTest
  static final ArchRule domainはapplicationに依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..application..")
          .as("Rule 4: domain層はapplication層にアクセスしてはならない");

  @ArchTest
  static final ArchRule domainはinfrastructureに依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..infrastructure..")
          .as("Rule 5: domain層はinfrastructure層にアクセスしてはならない");

  @ArchTest
  static final ArchRule domainはpresentationに依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..presentation..")
          .as("Rule 6: domain層はpresentation層にアクセスしてはならない");

  @ArchTest
  static final ArchRule infrastructureはpresentationに依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..infrastructure..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..presentation..")
          .as("Rule 7: infrastructure層はpresentation層にアクセスしてはならない");
}
