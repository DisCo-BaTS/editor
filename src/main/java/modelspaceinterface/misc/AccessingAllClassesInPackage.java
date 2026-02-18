package modelspaceinterface.misc;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.util.HashSet;
import java.util.Set;

public class AccessingAllClassesInPackage {

    public Set<Class> findAllClassesUsingReflectionsLibrary(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        return new HashSet<>(reflections.getSubTypesOf(Object.class));
    }

    public Set<Class> findAllClassesUsingClassGraphLibrary(String packageName) {
        try (ScanResult scanResult =
                     new ClassGraph()
                             .enableAllInfo()
                             .acceptPackages(packageName)
                             .scan()) {
            Set<Class> output = new HashSet<>();
            for (ClassInfo routeClassInfo : scanResult.getAllClasses()) {
                output.add(routeClassInfo.loadClass());
            }
            return output;
        }
    }
}
