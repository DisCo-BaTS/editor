package modelspaceinterface;

import handlers.ElementHandler;
import modelspaceinterface.misc.AccessingAllClassesInPackage;
import xml.XMLProfileReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class ModelConverter {

    public HashMap<String, Path> convert() {
        XMLProfileReader xmlProfileReader = new XMLProfileReader();
        ElementHandler elementHandler = ElementHandler.getInstance();
        elementHandler.setXMLData(xmlProfileReader.readXML());

        HashMap<String, String> code = generateCode(elementHandler);

        RuntimeCompiler runtimeCompiler = new RuntimeCompiler();
        HashMap<String, Path> paths = runtimeCompiler.createFiles(code);
        HashMap<String, Path> compiledPaths = runtimeCompiler.compileSources(paths);

        StringBuilder jbIndex = new StringBuilder();
        for (Map.Entry<String, Path> c : compiledPaths.entrySet()) {
            //Add line per file
            jbIndex.append(c.getKey()).append("\n");
        }
        //Create jaxb.index File
        Path p = Path.of(RuntimeCompiler.tmpPath);
        try {
            Path filePath = Files.writeString(p, jbIndex.toString());
            String s = Files.readString(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        generateJarFile(compiledPaths);

        return compiledPaths;
    }

    private ArrayList<String> getExistingClasses() {
        AccessingAllClassesInPackage aACIP = new AccessingAllClassesInPackage();
        Set<Class> s = aACIP.findAllClassesUsingClassGraphLibrary("library.model");
        ArrayList<String> clAL = new ArrayList<>();
        for (Class c : s) {
            clAL.add(c.getName().split("\\.")[c.getName().split("\\.").length - 1]);
        }
        return clAL;
    }

    public HashMap<String, String> generateCode(ElementHandler elementHandler) {
        CodeGenerator codeGenerator = new CodeGenerator(elementHandler);
        HashMap<String, String> output = new HashMap<>();
        ArrayList<String> existingClasses = getExistingClasses();

        for (String modelElement : elementHandler.getAllModelElementNames()) {
            if (existingClasses.contains(modelElement.split("<")[0])) {
                continue;
            }
            output.put(modelElement, codeGenerator.generateClass(elementHandler.getNamedElement(modelElement)));
        }

        return output;
    }

    public void generateJarFile(HashMap<String, Path> path) {
        try {
            String jarPath = path.values().toArray(new Path[]{})[0].getParent() + File.separator + "generated-classes-tssmeta.jar";
            FileOutputStream fout = new FileOutputStream(jarPath);
            JarOutputStream jarOut = new JarOutputStream(fout);
            for (Map.Entry<String, Path> entry : path.entrySet()) {
                jarOut.putNextEntry(new ZipEntry("library/model/" + entry.getKey() + ".class"));
                jarOut.write(Files.readAllBytes(entry.getValue()));
                jarOut.closeEntry();
            }
            jarOut.close();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
