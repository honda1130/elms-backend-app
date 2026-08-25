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
          .as("presentation.requestパッケージのクラスはRequestで終わること");

  @ArchTest
  static final ArchRule presentationResponseのクラスはResponseで終わること =
      classes()
          .that()
          .resideInAPackage("..presentation.response..")
          .should()
          .haveSimpleNameEndingWith("Response")
          .as("presentation.responseパッケージのクラスはResponseで終わること");

  @ArchTest
  static final ArchRule RestControllerのクラスはControllerで終わること =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .haveSimpleNameEndingWith("Controller")
          .as("@RestController付きクラスはControllerで終わること");

  @ArchTest
  static final ArchRule applicationServiceのインターフェースはApplicationServiceで終わること =
      classes()
          .that()
          .resideInAPackage("..application.service..")
          .and()
          .areInterfaces()
          .should()
          .haveSimpleNameEndingWith("ApplicationService")
          .as("application.serviceのインターフェースはApplicationServiceで終わること");

  @ArchTest
  static final ArchRule applicationServiceのServiceクラスはApplicationServiceImplで終わること =
      classes()
          .that()
          .resideInAPackage("..application.service..")
          .and()
          .areAnnotatedWith(Service.class)
          .should()
          .haveSimpleNameEndingWith("ApplicationServiceImpl")
          .as("application.serviceの@ServiceクラスはApplicationServiceImplで終わること");

  @ArchTest
  static final ArchRule applicationCommandのクラスはCommandで終わること =
      classes()
          .that()
          .resideInAPackage("..application.command..")
          .should()
          .haveSimpleNameEndingWith("Command")
          .as("application.commandパッケージのクラスはCommandで終わること");

  @ArchTest
  static final ArchRule applicationDtoのクラスはDtoで終わること =
      classes()
          .that()
          .resideInAPackage("..application.dto..")
          .should()
          .haveSimpleNameEndingWith("Dto")
          .as("application.dtoパッケージのクラスはDtoで終わること");

  @ArchTest
  static final ArchRule domainRepositoryのインターフェースはRepositoryで終わること =
      classes()
          .that()
          .resideInAPackage("..domain.repository..")
          .and()
          .areInterfaces()
          .should()
          .haveSimpleNameEndingWith("Repository")
          .as("domain.repositoryのインターフェースはRepositoryで終わること");

  @ArchTest
  static final ArchRule infrastructureRepositoryのRepositoryクラスはRepositoryImplで終わること =
      classes()
          .that()
          .resideInAPackage("..infrastructure.repository..")
          .and()
          .areAnnotatedWith(Repository.class)
          .should()
          .haveSimpleNameEndingWith("RepositoryImpl")
          .as("infrastructure.repositoryの@RepositoryクラスはRepositoryImplで終わること");

  @ArchTest
  static final ArchRule infrastructureDaoのクラスはDaoで終わること =
      classes()
          .that()
          .resideInAPackage("..infrastructure.dao..")
          .should()
          .haveSimpleNameEndingWith("Dao")
          .as("infrastructure.daoパッケージのクラスはDaoで終わること");

  @ArchTest
  static final ArchRule infrastructureEntityのクラスはEntityで終わること =
      classes()
          .that()
          .resideInAPackage("..infrastructure.entity..")
          .should()
          .haveSimpleNameEndingWith("Entity")
          .as("infrastructure.entityパッケージのクラスはEntityで終わること");

  @ArchTest
  static final ArchRule infrastructureRowのクラスはRowで終わること =
      classes()
          .that()
          .resideInAPackage("..infrastructure.row..")
          .should()
          .haveSimpleNameEndingWith("Row")
          .as("infrastructure.rowパッケージのクラスはRowで終わること");

  @ArchTest
  static final ArchRule RestControllerのクラスはRequestMappingを持つこと =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .beAnnotatedWith(RequestMapping.class)
          .as("@RestController付きクラスは@RequestMappingを持つこと");

  @ArchTest
  static final ArchRule RestControllerのクラスはTagアノテーションを持つこと =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .beAnnotatedWith(Tag.class)
          .as("@RestController付きクラスはSwaggerの@Tagを持つこと");

  @ArchTest
  static final ArchRule infrastructureRepositoryの非インターフェースクラスはRepositoryを持つこと =
      classes()
          .that()
          .resideInAPackage("..infrastructure.repository..")
          .and()
          .areNotInterfaces()
          .should()
          .beAnnotatedWith(Repository.class)
          .as("infrastructure.repositoryのクラス（インターフェース以外）は@Repositoryを持つこと");

  @ArchTest
  static final ArchRule domainServiceのインターフェースはDomainServiceで終わること =
      classes()
          .that()
          .resideInAPackage("..domain.service..")
          .and()
          .areInterfaces()
          .should()
          .haveSimpleNameEndingWith("DomainService")
          .as("domain.serviceのインターフェースはDomainServiceで終わること");

  @ArchTest
  static final ArchRule domainServiceの非インターフェースクラスはDomainServiceImplで終わること =
      classes()
          .that()
          .resideInAPackage("..domain.service..")
          .and()
          .areNotInterfaces()
          .should()
          .haveSimpleNameEndingWith("DomainServiceImpl")
          .as("domain.serviceのクラス（インターフェース以外）はDomainServiceImplで終わること");
}
