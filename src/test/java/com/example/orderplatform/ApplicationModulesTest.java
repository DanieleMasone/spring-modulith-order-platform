package com.example.orderplatform;

import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ApplicationModulesTest {

    private final ApplicationModules modules = ApplicationModules.of(
            OrderPlatformApplication.class,
            JavaClass.Predicates.resideInAPackage("..generated.."));

    @Test
    void verifiesApplicationModuleBoundaries() {
        modules.verify();
    }

    @Test
    void writesSpringModulithDocumentationSnippets() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeModuleCanvases();
    }
}
