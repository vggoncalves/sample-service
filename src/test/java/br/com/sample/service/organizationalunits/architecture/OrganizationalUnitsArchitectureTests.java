package br.com.sample.service.organizationalunits.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class OrganizationalUnitsArchitectureTests {
    private static final JavaClasses CLASSES = new ClassFileImporter().importPath(
            Path.of("target", "classes", "br", "com", "sample", "service", "organizationalunits"));

    @Test
    void dominioNaoDependeDasCamadasExternas() {
        noClasses().that().resideInAPackage("..domain..").should().dependOnClassesThat()
                .resideInAnyPackage("..api..", "..application..", "..infrastructure..").check(CLASSES);
    }

    @Test
    void aplicacaoNaoDependeDeApiOuInfraestrutura() {
        noClasses().that().resideInAPackage("..application..").should().dependOnClassesThat()
                .resideInAnyPackage("..api..", "..infrastructure..").check(CLASSES);
    }

    @Test
    void apiNaoDependeDaInfraestrutura() {
        noClasses().that().resideInAPackage("..api..").should().dependOnClassesThat()
                .resideInAPackage("..infrastructure..").check(CLASSES);
    }
}
