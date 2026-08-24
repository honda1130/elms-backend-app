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
  static final ArchRule presentation層はinfrastructure層に依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..presentation..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..infrastructure..")
          .as("presentation層はinfrastructure層にアクセスしてはならない");

  @ArchTest
  static final ArchRule presentation層はdomain層に依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..presentation..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..domain..")
          .as("presentation層はdomain層にアクセスしてはならない");

  @ArchTest
  static final ArchRule application層はinfrastructure層に依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..infrastructure..")
          .as("application層はinfrastructure層にアクセスしてはならない");

  @ArchTest
  static final ArchRule application層はpresentation層に依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..presentation..")
          .as("application層はpresentation層にアクセスしてはならない");

  @ArchTest
  static final ArchRule domain層はapplication層に依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..application..")
          .as("domain層はapplication層にアクセスしてはならない");

  @ArchTest
  static final ArchRule domain層はinfrastructure層に依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..infrastructure..")
          .as("domain層はinfrastructure層にアクセスしてはならない");

  @ArchTest
  static final ArchRule domain層はpresentation層に依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..presentation..")
          .as("domain層はpresentation層にアクセスしてはならない");

  @ArchTest
  static final ArchRule domainModel層はdomainService層に依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..domain.model..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..domain.service..")
          .as("domain.model層はdomain.service層にアクセスしてはならない");

  @ArchTest
  static final ArchRule infrastructure層はpresentation層に依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..infrastructure..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..presentation..")
          .as("infrastructure層はpresentation層にアクセスしてはならない");

  @ArchTest
  static final ArchRule infrastructure層はapplication層に依存していないこと =
      noClasses()
          .that()
          .resideInAPackage("..infrastructure..")
          .should()
          .accessClassesThat()
          .resideInAPackage("..application..")
          .as("infrastructure層はapplication層にアクセスしてはならない");
}
