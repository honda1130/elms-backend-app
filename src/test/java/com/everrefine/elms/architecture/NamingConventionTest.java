package com.everrefine.elms.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(
    packages = "com.everrefine.elms",
    importOptions = ImportOption.DoNotIncludeTests.class)
class NamingConventionTest {

  @ArchTest
  static final ArchRule presentationRequestのクラスはRequestで終わること =
      classes()
          .that()
          .resideInAPackage("..presentation.request..")
          .should()
          .haveSimpleNameEndingWith("Request")
          .as("Rule 8: presentation.requestパッケージのクラスはRequestで終わること");

  @ArchTest
  static final ArchRule RestControllerのクラスはControllerで終わること =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .haveSimpleNameEndingWith("Controller")
          .as("Rule 9: @RestController付きクラスはControllerで終わること");

  @ArchTest
  static final ArchRule applicationServiceのインターフェースはApplicationServiceで終わること =
      classes()
          .that()
          .resideInAPackage("..application.service..")
          .and()
          .areInterfaces()
          .should()
          .haveSimpleNameEndingWith("ApplicationService")
          .as("Rule 10: application.serviceのインターフェースはApplicationServiceで終わること");

  @ArchTest
  static final ArchRule applicationServiceのServiceクラスはApplicationServiceImplで終わること =
      classes()
          .that()
          .resideInAPackage("..application.service..")
          .and()
          .areAnnotatedWith(Service.class)
          .should()
          .haveSimpleNameEndingWith("ApplicationServiceImpl")
          .as("Rule 11: application.serviceの@ServiceクラスはApplicationServiceImplで終わること");

  @ArchTest
  static final ArchRule applicationCommandのクラスはCommandで終わること =
      classes()
          .that()
          .resideInAPackage("..application.command..")
          .should()
          .haveSimpleNameEndingWith("Command")
          .as("Rule 12: application.commandパッケージのクラスはCommandで終わること");

  @ArchTest
  static final ArchRule applicationDtoのクラスはDtoで終わること =
      classes()
          .that()
          .resideInAPackage("..application.dto..")
          .should()
          .haveSimpleNameEndingWith("Dto")
          .as("Rule 13: application.dtoパッケージのクラスはDtoで終わること");

  @ArchTest
  static final ArchRule domainRepositoryのインターフェースはRepositoryで終わること =
      classes()
          .that()
          .resideInAPackage("..domain.repository..")
          .and()
          .areInterfaces()
          .should()
          .haveSimpleNameEndingWith("Repository")
          .as("Rule 14: domain.repositoryのインターフェースはRepositoryで終わること");

  @ArchTest
  static final ArchRule infrastructureRepositoryのRepositoryクラスはRepositoryImplで終わること =
      classes()
          .that()
          .resideInAPackage("..infrastructure.repository..")
          .and()
          .areAnnotatedWith(Repository.class)
          .should()
          .haveSimpleNameEndingWith("RepositoryImpl")
          .as("Rule 15: infrastructure.repositoryの@RepositoryクラスはRepositoryImplで終わること");

  @ArchTest
  static final ArchRule RestControllerのクラスはRequestMappingを持つこと =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .beAnnotatedWith(RequestMapping.class)
          .as("Rule 16: @RestController付きクラスは@RequestMappingを持つこと");

  @ArchTest
  static final ArchRule RestControllerのクラスはTagアノテーションを持つこと =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .beAnnotatedWith(Tag.class)
          .as("Rule 17: @RestController付きクラスはSwaggerの@Tagを持つこと");

  @ArchTest
  static final ArchRule infrastructureRepositoryの非インターフェースクラスはRepositoryを持つこと =
      classes()
          .that()
          .resideInAPackage("..infrastructure.repository..")
          .and()
          .areNotInterfaces()
          .should()
          .beAnnotatedWith(Repository.class)
          .as("Rule 18: infrastructure.repositoryのクラス（インターフェース以外）は@Repositoryを持つこと");

  @ArchTest
  static final ArchRule domainServiceのインターフェースはDomainServiceで終わること =
      classes()
          .that()
          .resideInAPackage("..domain.service..")
          .and()
          .areInterfaces()
          .should()
          .haveSimpleNameEndingWith("DomainService")
          .as("Rule 19: domain.serviceのインターフェースはDomainServiceで終わること");

  @ArchTest
  static final ArchRule domainServiceの非インターフェースクラスはDomainServiceImplで終わること =
      classes()
          .that()
          .resideInAPackage("..domain.service..")
          .and()
          .areNotInterfaces()
          .should()
          .haveSimpleNameEndingWith("DomainServiceImpl")
          .as("Rule 20: domain.serviceのクラス（インターフェース以外）はDomainServiceImplで終わること");
}
