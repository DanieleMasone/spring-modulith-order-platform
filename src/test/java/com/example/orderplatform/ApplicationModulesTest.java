package com.example.orderplatform;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ApplicationModulesTest {

    private final ApplicationModules modules = ApplicationModules.of(
            OrderPlatformApplication.class,
            JavaClass.Predicates.resideInAPackage("..generated.."));
    private final JavaClasses productionClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.example.orderplatform");

    @Test
    void verifiesApplicationModuleBoundaries() {
        modules.verify();
    }

    @Test
    void domainPackagesStayFrameworkIndependent() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "org.springframework.data..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "com.example.orderplatform.generated..",
                        "com.example.orderplatform..infrastructure..")
                .check(productionClasses);
    }

    @Test
    void applicationPackagesDoNotDependOnInfrastructureAdapters() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(productionClasses);
    }

    @Test
    void writesSpringModulithDocumentationSnippets() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeModuleCanvases();
    }
}
