package modelspaceinterface;

import handlers.ElementHandler;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import model.EnumLiteral;
import model.InheretedClass;
import model.ModelAttribute;
import model.ModelElement;
import model.ModelEnum;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

public class CodeGenerator {

    public CodeGenerator(ElementHandler elementHandler) {
        this.elementHandler = elementHandler;
        packages = new ArrayList<>();
        Package[] packages1 = Package.getPackages();
        for (Package p : packages1) {
            if (p.getName().startsWith("library.model")) {
                packages.add(p.getName() + ".");
            }
        }
    }

    private final ElementHandler elementHandler;

    public String generateEnum(ModelEnum modelEnum) {
        StringBuilder sb = new StringBuilder("public enum ");
        sb.append(modelEnum.name).append("{\n");
        for (EnumLiteral s : modelEnum.enumLiterals) {
            sb.append("\t").append(s).append(",\n");
        }
        sb.append("}\n");
        sb.insert(0, "package library.model;\n\n");
        return sb.toString();
    }

    private boolean doesExtend(ModelElement modelElement) {
        if (modelElement.getClass() == InheretedClass.class && ((InheretedClass) modelElement).getSuperClass() != null
                && !((InheretedClass) modelElement).getSuperClass().equals("")) {
            return true;
        } else if (!modelElement.dependency.isEmpty()
                && !(elementHandler.getElement(modelElement.dependency.get(0)) == null)
                && !elementHandler.getElement(modelElement.dependency.get(0)).isMetaClass) {
            return true;
        }
        return false;
    }

    private final ArrayList<String> packages;

    private final HashMap<String, String> dataTypes = new HashMap<>() {
        {
            put("byte", "0");
            put("short", "0");
            put("int", "0");
            put("long", "0L");
            put("float", "0.0f");
            put("double", "0.0d");
            put("char", "'\u0000'");
            put("boolean", "false");
        }
    };

    public String generateClass(ModelElement modelElement) {
        if (modelElement.getClass() == ModelEnum.class) {
            return generateEnum((ModelEnum) modelElement);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("import lombok.Data;\nimport javax.xml.bind.annotation.XmlRootElement;\n");
        sb.append("import lombok.EqualsAndHashCode;\n\nimport library.model.simulation.*;\n");
        sb.append("import library.model.simulation.enums.*;\nimport library.model.simulation.units.*;\n");
        sb.append("import library.model.traffic.*;\n\n");
        sb.append("@XmlRootElement\n");
        sb.append("@Data\n");
        if (doesExtend(modelElement)) {
            sb.append("@EqualsAndHashCode(callSuper = true)\n");
        }
        sb.append("public class ").append(modelElement.name);
        ArrayList<String> genericTypes = new ArrayList<>();
        if (modelElement.name.split("<").length > 1) {
            String[] splitted = modelElement.name.split("<");
            for (String split2 : splitted[1].split(",")) {
                if (split2.endsWith(">")) {
                    genericTypes.add(split2.substring(0, split2.length() - 1));
                    break;
                }
                genericTypes.add(split2);
            }
        }
        String superClassName = generateExtendsAndGetSuperClassName(modelElement, sb);

        sb.append(" {\n\n");
        for (ModelAttribute modelAttribute : modelElement.attributes.values()) {
            String visibility = "";
            if (!modelAttribute.visibility.equals("package")) {
                visibility = modelAttribute.visibility;
            }
            ModelElement type = null;
            if (elementHandler.getElement(modelAttribute.type) != null) {
                type = elementHandler.getElement(elementHandler.getElement(modelAttribute.type).id);
            }
            StringBuilder typeName = null;
            if (!genericTypes.isEmpty()) {
                for (String sString : modelAttribute.type.split("_")) {
                    if (genericTypes.contains(sString)) {
                        typeName = new StringBuilder(sString);
                        break;
                    }
                }
            }
            //fallback if custom generic classes are used without generic
            String gctName = null;
            if (type == null) {
                gctName = elementHandler.findGenericClassByName(modelAttribute.type);
            }
            boolean multiTypeTypeNameSet = false;
            if (type == null && typeName == null) {
                if (DefaultDataTypes.endsWithAnyPrimitiveDatatype(modelAttribute.type)) {
                    typeName = new StringBuilder(DefaultDataTypes.getPrimitiveDatatype(modelAttribute.type));
                } else if (DefaultDataTypes.containsAnySpecialDatatype(modelAttribute.type)) {
                    String[] specialDatatype = DefaultDataTypes.getSpecialDatatype(modelAttribute.type);
                    ArrayList<String> subtypes = DefaultDataTypes.getContainedDatatypes(modelAttribute.type, null);
                    typeName = new StringBuilder(specialDatatype[0] + "<");
                    for (int i = 0; i < subtypes.size(); i++) {
                        if (i > 0) {
                            typeName.append(",");
                        }
                        String type2FillIn = subtypes.get(i);
                        if (type2FillIn.split("<").length > 1) {
                            String subSubType = type2FillIn.split("<")[1].split(">")[0];
                            if (!genericTypes.contains(subSubType) && elementHandler.getNamedElement(subSubType) == null) {
                                type2FillIn = type2FillIn.split("<")[0]; // + "<>";
                            }
                        }
                        typeName.append(type2FillIn);
                    }
                    typeName.append(">");
                    sb.insert(0, "import java.util." + specialDatatype[0] + ";\n");
                }
                if (typeName == null) {
                    typeName = new StringBuilder("Object");
                }
            } else if (typeName == null) {
                ArrayList<String> modelParts = elementHandler.getPartOfModel(modelAttribute.type);
                if (modelParts.size() > 1) {
                    multiTypeTypeNameSet = true;
                    typeName = new StringBuilder(modelParts.get(0));
                    typeName.append("<");
                    for (int i = 1; i < modelParts.size(); i++) {
                        if (i > 1) {
                            typeName.append(",");
                        }
                        typeName.append(modelParts.get(i));
                    }
                    typeName.append(">");
                } else {
                    typeName = new StringBuilder(type.name);
                }
            }
            boolean arr = false;
            boolean list = false;
            if (gctName != null) {
                typeName = new StringBuilder(gctName);
            }

            if (typeName.toString().split("<").length > 1 && !multiTypeTypeNameSet) {
                String subSubType = typeName.toString().split("<")[1].split(">")[0];
                if (!genericTypes.contains(subSubType) && elementHandler.getNamedElement(subSubType) == null) {
                    typeName = new StringBuilder(typeName.toString().split("<")[0]);
                }
            }

            if (modelAttribute.maxMultiplicity > 1) {
                typeName.append("[]");
                arr = true;
            } else if (modelAttribute.maxMultiplicity == -1) {
                typeName = new StringBuilder("ArrayList<" + typeName + ">");
                list = true;
                sb.insert(0, "import java.util.ArrayList;\n");
            }
            sb.append("\t").append(visibility).append(" ").append(typeName).append(" ").append(modelAttribute.name);
            if (arr) {
                String x = typeName.toString();
                x = x.replace("[]", "[" + modelAttribute.maxMultiplicity + "]");
                sb.append(" = ").append("new ").append(x);
            } else if (list) {
                sb.append(" = ").append("new ArrayList<>()");
            }
            sb.append(";\n");
        }

        if ((modelElement.getClass() == InheretedClass.class && ((InheretedClass) modelElement).getSuperClass() != null
                && !((InheretedClass) modelElement).getSuperClass().equals(""))
                || (!modelElement.dependency.isEmpty() && !(elementHandler.getElement(modelElement.dependency.get(0)) == null)
                && !elementHandler.getElement(modelElement.dependency.get(0)).isMetaClass)) {
            Class c = getClassObject(superClassName);

            if (c != null) {
                for (Method m : c.getMethods()) {
                    if (Modifier.isAbstract(m.getModifiers())) {
                        sb.append(generateDefaultMethodImplementation(m));
                    }
                }
            }
        }

        sb.append("\n}\n");
        sb.insert(0, "package library.model;\n\n");

        return sb.toString();
    }

    private String generateExtendsAndGetSuperClassName(ModelElement modelElement, StringBuilder sb) {
        if (modelElement.getClass() == InheretedClass.class && ((InheretedClass) modelElement).getSuperClass() != null
                && !((InheretedClass) modelElement).getSuperClass().equals("")) {
            sb.append(" extends ");
            if (modelElement.hasGenericType) {
                sb.append(((InheretedClass) modelElement).getSuperClass());
            } else {
                sb.append(((InheretedClass) modelElement).getSuperClass().split("<")[0]);
            }
            sb.insert(0, getExtensionImport(((InheretedClass) modelElement).getSuperClass().split("<")[0]) + "\n\n");
            return ((InheretedClass) modelElement).getSuperClass();
        } else if (!modelElement.dependency.isEmpty()
                && !(elementHandler.getElement(modelElement.dependency.get(0)) == null)
                && !elementHandler.getElement(modelElement.dependency.get(0)).isMetaClass) {
            sb.append(" extends ");
            if (modelElement.hasGenericType) {
                sb.append(elementHandler.getElement(modelElement.dependency.get(0)).name);
            } else {
                sb.append(elementHandler.getElement(modelElement.dependency.get(0)).name.split("<")[0]);
            }
            sb.insert(0, getExtensionImport(elementHandler.getElement(modelElement.dependency.get(0)).name.split("<")[0]) + "\n\n");
            return elementHandler.getElement(modelElement.dependency.get(0)).name;
        }
        return "";
    }

    private Class getClassObject(String superClassName) {
        Class c = null;

        for (String p : packages) {
            try {
                c = Class.forName(p + superClassName.split("<")[0]);
            } catch (ClassNotFoundException ignored) {

            }
            if (c != null) {
                break;
            }
        }

        return c;
    }

    //TODO implement case for return type void
    private String generateDefaultMethodImplementation(Method m) {
        StringBuilder sb = new StringBuilder();

        String modifierA;
        if (Modifier.isPublic(m.getModifiers())) {
            modifierA = "public";
        } else if (Modifier.isPrivate(m.getModifiers())) {
            modifierA = "private";
        } else if (Modifier.isProtected(m.getModifiers())) {
            modifierA = "protected";
        } else {
            modifierA = "";
        }
        sb.append("\n\t").append(modifierA).append(" ");
        if (Modifier.isStatic(m.getModifiers())) {
            sb.append("static ");
        }
        if (Modifier.isFinal(m.getModifiers())) {
            sb.append("final ");
        }
        sb.append(m.getReturnType().getName()).append(" ").append(m.getName()).append("(");

        int character = 97;
        int params = m.getParameterTypes().length;
        for (Class d : m.getParameterTypes()) {
            char param = (char) character;
            character++;
            params--;

            sb.append(d.getName()).append(" ").append(param);
            if (params > 0) {
                sb.append(", ");
            }
        }

        sb.append(") {\n\t\treturn ");

        if (m.getReturnType().getName().charAt(0) >= 65 && m.getReturnType().getName().charAt(0) <= 90) {
            sb.append("null");
        } else sb.append(dataTypes.getOrDefault(m.getReturnType().getName(), "null"));

        sb.append(";\n\t}\n");

        return sb.toString();
    }

    private static String getExtensionImport(String clazz) {
        try (ScanResult scanResult = new ClassGraph().acceptPackages("library.model")
                .enableClassInfo().scan()) {
            java.util.List<Class<Object>> clazzes = scanResult
                    .getSubclasses(Object.class.getName())
                    .loadClasses(Object.class);
            for (Class<Object> objectClass : clazzes) {
                if (clazz.equals(objectClass.getSimpleName())) {
                    return "import " + objectClass.getName() + ";";
                }
            }
        }
        return "";
    }
}
