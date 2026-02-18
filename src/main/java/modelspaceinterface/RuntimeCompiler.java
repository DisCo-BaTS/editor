package modelspaceinterface;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RuntimeCompiler {

    public static String tmpPath = System.getProperty("java.io.tmpdir") + File.separator + "tssmeta" + File.separator + "library" + File.separator + "model";

    public HashMap<String, Path> createFiles(HashMap<String, String> code) {
        HashMap<String, Path> paths = new HashMap<>();
        for (Map.Entry<String, String> clazz : code.entrySet()) {
            Map.Entry<String, Path> file = createFile(clazz.getKey(), clazz.getValue());
            paths.put(file.getKey(), file.getValue());
        }
        return paths;
    }

    public Map.Entry<String, Path> createFile(String name, String code) {
        String folder = tmpPath;
        File ff = new File(folder);
        ff.mkdirs();
        String tmpProperty = ff.getAbsolutePath();
        Path sourcePath = Paths.get(tmpProperty, name.split("<")[0] + ".java");
        try {
            Files.writeString(sourcePath, code);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AbstractMap.SimpleEntry<>(name, sourcePath);
    }

    public HashMap<String, Path> compileSources(HashMap<String, Path> javaFiles) {
        HashMap<String, Path> output = new HashMap<>();
        JavaCompiler compiler2 = ToolProvider.getSystemJavaCompiler();
        ArrayList<String> paths = new ArrayList<>();
        javaFiles.values().forEach(t -> paths.add(t.toFile().getAbsolutePath()));
        String[] pa = paths.toArray(new String[]{});
        compiler2.run(null, null, null, pa);

        StringBuilder jbIndex = new StringBuilder();
        for (Map.Entry<String, Path> p : javaFiles.entrySet()) {
            Path test = p.getValue().getParent().resolve(p.getKey().split("<")[0] + ".class");
            if (test.toFile().exists()) {
                output.put(p.getKey(), test);
                //Add line per file
                jbIndex.append(p.getKey().split("<")[0]).append("\n");
            }
        }

        //Create jaxb.index File
        Path p = Path.of(RuntimeCompiler.tmpPath + File.separator + "jaxb.index");
        try {
            Files.writeString(p, jbIndex.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }

    public Map.Entry<String, Path> compileSource(String name, Path javaFile) {
        JavaCompiler compiler2 = ToolProvider.getSystemJavaCompiler();
        String[] pa = new String[]{javaFile.toFile().getAbsolutePath()};
        compiler2.run(null, null, null, pa);

        Path resultPath = javaFile.getParent().resolve(name.split("<")[0] + ".class");
        return new AbstractMap.SimpleEntry<>(name, resultPath);
    }

    public Map.Entry<String, Path> compileSource(Map.Entry<String, Path> generatedScenario) {
        return compileSource(generatedScenario.getKey(), generatedScenario.getValue());
    }

    public HashMap<String, Path> compileCode(HashMap<String, String> javaCode) {
        return compileSources(createFiles(javaCode));
    }
}
