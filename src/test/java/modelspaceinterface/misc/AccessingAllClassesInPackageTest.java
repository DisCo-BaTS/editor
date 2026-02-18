package modelspaceinterface.misc;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;

class AccessingAllClassesInPackageTest {

    @Test
    void test() {
        String pkg = "library.model";
        try (ScanResult scanResult =
                     new ClassGraph()                 // Log to stderr
                             .enableAllInfo()         // Scan classes, methods, fields, annotations
                             .acceptPackages(pkg)     // Scan com.xyz and subpackages (omit to scan all packages)
                             .scan()) {               // Start the scan
            for (ClassInfo routeClassInfo : scanResult.getAllClasses()) {
                System.out.println(routeClassInfo.getName());
            }
        }
    }

}