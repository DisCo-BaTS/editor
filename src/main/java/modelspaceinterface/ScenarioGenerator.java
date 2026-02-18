package modelspaceinterface;

import handlers.ElementHandler;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import library.model.dto.scenario.ScenarioDTO;
import model.InstanciatedClass;
import model.ModelAttribute;
import model.ModelEnum;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ScenarioGenerator {

    public static ScenarioDTO generateScenario(ElementHandler elementHandler) {
        //Generate Code
        //1. For the scenario
        String code = generateCode(elementHandler.getInstanciatedClasses(), elementHandler);
        //2. For all other classes
        HashMap<String, String> allClassesCode = getOtherCode();
        allClassesCode.put("GeneratedScenario", code);

        //Compile
        RuntimeCompiler runtimeCompiler = new RuntimeCompiler();
        HashMap<String, Path> compiledClasses = runtimeCompiler.compileSources(runtimeCompiler.createFiles(allClassesCode));

        //Instanciate
        ScenarioDTO scenario = null;
        try {
            scenario = createInstance(compiledClasses.get("GeneratedScenario"));
            (new ModelConverter()).generateJarFile(compiledClasses);
        } catch (MalformedURLException | ClassNotFoundException | InvocationTargetException
                | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        // return object
        return scenario;
    }

    private static int counter;

    private static int getInt() {
        counter++;
        return counter - 1;
    }

    private static HashMap<String, String> getOtherCode() {
        ModelConverter modelConverter = new ModelConverter();
        return modelConverter.generateCode(ElementHandler.getInstance());
    }

    private static String generateCode(ArrayList<InstanciatedClass> instanciatedClasses, ElementHandler elementHandler) {
        ArrayList<String> usedClasses = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("""
                import library.model.dto.scenario.ScenarioDTO;
                import library.model.simulation.*;
                import library.model.simulation.enums.*;
                import library.model.simulation.units.*;
                                
                import java.util.ArrayList;

                public class GeneratedScenario {

                    public static ScenarioDTO getScenario() {
                """);
        counter = 0;
        ArrayList<Integer> excludedInstances = new ArrayList<>();
        HashMap<InstanciatedClass, Integer> instanciatedClassToNumberedInstance = new HashMap<>();
        for (InstanciatedClass instanciatedClass : instanciatedClasses) {
            int x = getInt();
            instanciatedClassToNumberedInstance.put(instanciatedClass, x);
            if (!elementHandler.extendsSimObj(instanciatedClass.getInstanceOf())) {
                excludedInstances.add(x);
            }
        }

        stringBuilder.append("\n\t\t// --- Creating needed Instances ---\n");

        HashMap<String, InstanciatedClass> setDefaultValues = new HashMap<>();

        //Filling instances with default values
        for (InstanciatedClass instanciatedClass : instanciatedClasses) {
            for (Map.Entry<String, ModelAttribute> attr : elementHandler.getAllAttributesOfModelElement(instanciatedClass.getInstanceOf(), null).entrySet()) {
                if (instanciatedClass.getAttribute(attr.getKey()) == null && instanciatedClass.getInstanceOf().getDefaultValue(attr.getValue().name) != null) {
                    if (instanciatedClass.getInstanceOf().getDefaultValue(attr.getValue().name).getClass() == String.class
                            && instanciatedClass.getInstanceOf().getDefaultValue(attr.getValue().name).equals("")) {
                        continue;
                    }
                    if (instanciatedClass.getInstanceOf().getDefaultValue(attr.getValue().name).getClass() == String.class
                            && elementHandler.getInstanceByName((String) instanciatedClass.getInstanceOf().getDefaultValue(attr.getValue().name)) != null) {
                        instanciatedClass.setNamedAttribute(attr.getValue().name,
                                elementHandler.getInstanceByName((String) instanciatedClass.getInstanceOf().getDefaultValue(attr.getValue().name)), null);
                    } else {
                        instanciatedClass.setNamedAttribute(attr.getValue().name,
                                instanciatedClass.getInstanceOf().getDefaultValue(attr.getValue().name), null);
                    }
                    setDefaultValues.put(attr.getValue().name, instanciatedClass);
                }
            }
        }


        //Instance Creation
        for (InstanciatedClass instanciatedClass : instanciatedClasses) {
            int noOfInstance = instanciatedClassToNumberedInstance.get(instanciatedClass);

            usedClasses.add(instanciatedClass.getInstanceOf().name.split("<")[0]);

            stringBuilder.append("\t\t").append(instanciatedClass.getInstanceOf().name.split("<")[0]);
            //if und so
            if (!instanciatedClass.getSubTypes().isEmpty()) {
                stringBuilder.append("<");
                for (int i = 0; i < instanciatedClass.getSubTypes().size(); i++) { //String s : instanciatedClass.getSubTypes()) {
                    if (i > 0) {
                        stringBuilder.append(",");
                    }
                    stringBuilder.append(instanciatedClass.getSubTypes().get(i));
                }
                stringBuilder.append(">");
            }

            stringBuilder.append(" ").append("instance")
                    .append(noOfInstance).append(" = new ");

            if (instanciatedClass.getInstanceOf().name.split("<").length > 1) {
                stringBuilder.append(instanciatedClass.getInstanceOf().name.split("<")[0]).append("<>");
            } else {
                stringBuilder.append(instanciatedClass.getInstanceOf().name);
            }
            stringBuilder.append("();\n");
        }

        counter = 0;

        int lists = 0;

        //Value assignment
        stringBuilder.append("\n\t\t// --- Setting attribute and instance values ---\n");
        for (InstanciatedClass instanciatedClass : instanciatedClasses) {
            int noOfInstance = instanciatedClassToNumberedInstance.get(instanciatedClass);
            for (Map.Entry<String, ModelAttribute> attr : elementHandler.getAllAttributesOfModelElement(instanciatedClass.getInstanceOf(), null).entrySet()) {
                if (instanciatedClass.getAttribute(attr.getKey()) != null) {
                    Object o = instanciatedClass.getAttribute(attr.getKey());
                    if (elementHandler.getElement(attr.getValue().type) != null
                            && elementHandler.getElement(attr.getValue().type).getClass() == ModelEnum.class) {
                        ModelEnum modelEnum = (ModelEnum) elementHandler.getElement(attr.getValue().type);
                        if (o.getClass() == HashMap.class) {
                            stringBuilder.append("\t\tinstance").append(noOfInstance).append(".set")
                                    .append(attr.getValue().name.substring(0, 1).toUpperCase())
                                    .append(attr.getValue().name.substring(1)).append("(")
                                    .append(modelEnum.name).append(".").append(((HashMap<?, ?>) o).get("value")).append(
                                            ");\n");
                        } else {
                            stringBuilder.append("\t\tinstance").append(noOfInstance).append(".set")
                                    .append(attr.getValue().name.substring(0, 1).toUpperCase())
                                    .append(attr.getValue().name.substring(1)).append("(")
                                    .append(modelEnum.name).append(".").append(o).append(");\n");
                        }
                    } else if (o.getClass().getName().equals("model.InstanciatedClass")) {
                        stringBuilder.append("\t\tinstance").append(noOfInstance).append(".set")
                                .append(attr.getValue().name.substring(0, 1).toUpperCase())
                                .append(attr.getValue().name.substring(1)).append("(")
                                .append("instance")
                                .append(instanciatedClassToNumberedInstance.get((InstanciatedClass) o)).append(");\n");
                    } else if (DefaultDataTypes.endsWithAnyPrimitiveDatatype(attr.getValue().type) || (!instanciatedClass.getSubTypes().isEmpty() && DefaultDataTypes.endsWithAnyPrimitiveDatatype(instanciatedClass.getSubTypes().get(0)))) {
                        stringBuilder.append("\t\tinstance").append(noOfInstance).append(".set")
                                .append(attr.getValue().name.substring(0, 1).toUpperCase())
                                .append(attr.getValue().name.substring(1)).append("(");
                        if ((DefaultDataTypes.getPrimitiveDatatype(attr.getValue().type) != null && DefaultDataTypes.getPrimitiveDatatype(attr.getValue().type).equals("String"))
                                || (!instanciatedClass.getSubTypes().isEmpty() && DefaultDataTypes.getPrimitiveDatatype(instanciatedClass.getSubTypes().get(0)).equals("String"))) {
                            stringBuilder.append("\"").append(o).append("\");\n");
                        } else {
                            stringBuilder.append(o).append(");\n");
                        }
                    } else if (o.getClass() == HashMap.class && ((HashMap<?, ?>) o).containsKey("value") && elementHandler.getInstanceByName(((String) ((HashMap<?, ?>) o).get("value"))) != null) {
                        int instNo = instanciatedClassToNumberedInstance.get(elementHandler.getInstanceByName(((String) ((HashMap<?, ?>) o).get("value"))));
                        stringBuilder.append("\t\tinstance").append(noOfInstance).append(".set")
                                .append(attr.getValue().name.substring(0, 1).toUpperCase())
                                .append(attr.getValue().name.substring(1)).append("(")
                                .append("instance").append(instNo).append(");\n");
                    } else if (o.getClass() == ArrayList.class) {
                        stringBuilder.append("\t\tArrayList list").append(lists).append(" = new ArrayList();\n");
                        for (Object o2 : (ArrayList) o) {
                            if (o2.getClass() == InstanciatedClass.class) {
                                stringBuilder.append("\t\tlist").append(lists).append(".add(").append("instance").append(instanciatedClassToNumberedInstance.get(o2)).append(");\n");
                            }
                        }
                        stringBuilder.append("\t\tinstance").append(noOfInstance).append(".set")
                                .append(attr.getValue().name.substring(0, 1).toUpperCase())
                                .append(attr.getValue().name.substring(1)).append("(")
                                .append("list").append(lists).append(");\n");
                        lists++;
                        //Should be the last option
                    } else if (o.getClass() == String.class) {
                        stringBuilder.append("\t\tinstance").append(noOfInstance).append(".set")
                                .append(attr.getValue().name.substring(0, 1).toUpperCase())
                                .append(attr.getValue().name.substring(1)).append("(")
                                .append("\"").append(o).append("\"").append(");\n");
                    }
                }
            }
        }

        Integer[] instanceArr = instanciatedClassToNumberedInstance.values().toArray(new Integer[]{});
        Arrays.sort(instanceArr);
        //Scenario Object Creation
        stringBuilder.append("\n\t\t// --- Creating Scenario Object ---\n").append("\t\tScenarioDTO scenario = new ScenarioDTO();\n");
        for (Integer i : instanceArr) {
            if (!excludedInstances.contains(i)) {
                stringBuilder.append("\t\tscenario.addSimulationObject(instance").append(i).append(");\n");
            }
        }

        stringBuilder.append("\n");

        for (Map.Entry<String, Object> entry : elementHandler.getScenarioConfig().entrySet()) {
            stringBuilder.append("\t\tscenario").append(".set")
                    .append(entry.getKey().substring(0, 1).toUpperCase())
                    .append(entry.getKey().substring(1)).append("(");

            if (entry.getKey().equals("library")) {
                stringBuilder.append("\"").append(entry.getValue()).append("\"");
            } else {
                stringBuilder.append(entry.getValue());
            }

            stringBuilder.append(");\n");
        }

        stringBuilder.append("""
                    
                        return scenario;
                    }
                }""");


        ArrayList<String> result = getImports(usedClasses);
        for (String s : result) {
            stringBuilder.insert(0, s + "\n");
        }

        stringBuilder.insert(0, "package library.model;\n\n");

        //unset default values
        for (Map.Entry<String, InstanciatedClass> entry : setDefaultValues.entrySet()) {
            entry.getValue().removeAttributeValue(entry.getKey(), null);
        }

        return stringBuilder.toString();
    }

    private static ScenarioDTO createInstance(Path path) throws MalformedURLException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        URL classUrl = path.getParent().getParent().getParent().toFile().toURI().toURL();
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classUrl});
        Class<?> clazz = Class.forName("library.model.GeneratedScenario", true, classLoader);
        Method m = clazz.getMethod("getScenario");
        Object o = m.invoke(null);

        return (ScenarioDTO) o;
    }

    private static ArrayList<String> getImports(ArrayList<String> classes) {
        ArrayList<String> output = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().acceptPackages("library.model")
                .enableClassInfo().scan()) {
            java.util.List<Class<Object>> clazzes = scanResult
                    .getSubclasses(Object.class.getName())
                    .loadClasses(Object.class);
            for (Class<Object> objectClass : clazzes) {
                if (classes.contains(objectClass.getSimpleName())) {
                    output.add("import " + objectClass.getName() + ";");
                }
            }

        }

        return output;
    }
}
