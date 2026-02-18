package handlers;

import gui.ModelElementType;
import gui.Tab;
import gui.modelelements.ModelElementPanel;
import model.InheretedClass;
import model.InstanciatedClass;
import model.ModelAttribute;
import model.ModelElement;
import model.ModelEnum;
import modelspaceinterface.DefaultDataTypes;
import xml.XMLData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ElementHandler {
    private static ElementHandler elementHandler;
    private LinkedHashMap<String, Object> scenarioConfig;

    public static ElementHandler getInstance() {
        if (elementHandler == null) {
            elementHandler = new ElementHandler();
        }
        return elementHandler;
    }

    private XMLData xmlData;
    private final ArrayList<InheretedClass> inheretedClasses;
    private final ArrayList<InstanciatedClass> instanciatedClasses;
    private final ArrayList<Tab> tabs;

    public void clear() {
        inheretedClasses.clear();
        instanciatedClasses.clear();
        tabs.clear();

        ArrayList<String> keys = new ArrayList<>();
        for (Map.Entry<String, ModelElement> modelElement : xmlData.elements.entrySet()) {
            if (modelElement.getValue().getClass() == ModelEnum.class && ((ModelEnum) modelElement.getValue()).isAddedEnum()) {
                keys.add(modelElement.getKey());
            }
        }
        keys.forEach(xmlData.elements::remove);
    }

    public void addInstance(InstanciatedClass instanciatedClass) {
        instanciatedClasses.add(instanciatedClass);
    }

    public void addExtension(InheretedClass inheretedClass) {
        inheretedClasses.add(inheretedClass);
    }

    public ArrayList<InstanciatedClass> getInstanciatedClasses() {
        return instanciatedClasses;
    }

    public ArrayList<Tab> getTabs() {
        return tabs;
    }

    public void addTab(Tab tab) {
        tabs.add(tab);
    }

    public void removeTab(Tab tab) {
        tabs.remove(tab);
    }

    public ArrayList<ModelEnum> getModelEnums() {
        ArrayList<ModelEnum> modelEnums = new ArrayList<>();
        for(ModelElement modelElement : xmlData.elements.values()) {
            if(modelElement.id.equals("ENUMDUMMY")) {
                modelEnums.add((ModelEnum) modelElement);
            }
        }
        return modelEnums;
    }

    public void setXMLData(XMLData xmlData) {
        if (this.xmlData == null) {
            this.xmlData = xmlData;
        }
    }

    private ElementHandler() {
        this.inheretedClasses = new ArrayList<>();
        this.instanciatedClasses = new ArrayList<>();
        this.tabs = new ArrayList<>();
        this.scenarioConfig = new LinkedHashMap<>() {{
            put("timeLimited", "false");
            put("maxDuration", "-1");
            put("iterationsLimited", "true");
            put("maxIterations", "10");
            put("library", "");
        }};
    }

    public ArrayList<String> getAllModelElementNames() {
        ArrayList<String> output = new ArrayList<>();
        if (xmlData != null) {
            for (ModelElement modelElement : xmlData.elements.values()) {
                if (!modelElement.isMetaClass) {
                    output.add(modelElement.name);
                }
            }
        }
        for (ModelElement modelElement : inheretedClasses) {
            if (!modelElement.isMetaClass) {
                output.add(modelElement.name);
            }
        }
        return output;
    }

    public InheretedClass extendClass(String superClassName, String newClassName) {
        InheretedClass inheretedClass = new InheretedClass();
        inheretedClass.name = newClassName;
        inheretedClass.type = "Class";
        String id = UUID.randomUUID().toString();
        id = "ID_" + id.replace("-", "_").toUpperCase();
        inheretedClass.id = id;
        inheretedClass.setSuperClass(superClassName);

        inheretedClasses.add(inheretedClass);

        return inheretedClass;
    }

    public ModelElement getNamedElement(String name) {
        for (InheretedClass ic : inheretedClasses) {
            if (ic.name.split("<")[0].equals(name.split("<")[0])) {
                return ic;
            }
        }
        for (ModelElement xd : xmlData.elements.values()) {
            if (xd.name.split("<")[0].equals(name.split("<")[0])) {
                return xd;
            }
        }
        return null;
    }

    public ModelElement getElement(String id) {
        if (id.equals("ENUMDUMMY")) {
            ModelElement modelElement = new ModelEnum();
            modelElement.name = "Enum";
            modelElement.id = "ENUMDUMMY";
            return modelElement;
        }
        for (InheretedClass ic : inheretedClasses) {
            if (ic.id.equals(id)) {
                return ic;
            }
        }
        if (xmlData.elements.containsKey(id)) {
            return xmlData.elements.get(id);
        }
        // Fallback if multiplicity is coded into name instead of in the proper fields
        for (String sString : id.split("_")) {
            if (getNamedElement(sString) != null) {
                return getNamedElement(sString);
            }
        }
        return null;
    }

    public static String getID() {
        String id = UUID.randomUUID().toString();
        id = "ID_" + id.replace("-", "_").toUpperCase();
        return id;
    }

    public boolean isPartOfModel(String fullName) {
        for (String modelNames : getAllModelElementNames()) {
            for (String name : fullName.split("_")) {
                if (name.startsWith(modelNames.split("<")[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<String> getPartOfModel(String fullName) {
        ArrayList<String> output = new ArrayList<>();
        for (String name : fullName.split("_")) {
            for (String modelNames : getAllModelElementNames()) {
                if (name.split("<")[0].equals(modelNames.split("<")[0])) {
                    output.add(modelNames.split("<", 2)[0]);
                    output = getRecursiveSubTypes(name, output);
                }
            }
        }
        return output;
    }

    public ArrayList<String> getSubTypesAfterMainType(String mainType, String fullString) {
        ArrayList<String> output = new ArrayList<>();
        fullString = fullString.split(mainType, 2)[1];
        for (String s : fullString.split("_")) {
            output.add(s);
        }
        return output;
    }

    private ArrayList<String> getRecursiveSubTypes(String fullName, ArrayList<String> output) {
        String name = fullName.substring(output.get(output.size() - 1).length());
        if (name.startsWith("<")) {
            name = name.substring(1);
        }
        if (name.endsWith(">")) {
            name = name.substring(0, name.length() - 1);
        }
        for (String modelNames : getAllModelElementNames()) {
            if (name.startsWith(modelNames.split("<")[0])) {
                output.add(modelNames.split("<")[0]);
                return getRecursiveSubTypes(name, output);
            }
        }
        if (DefaultDataTypes.startsWithPrimitiveDataType(name) != null) {
            output.add(DefaultDataTypes.startsWithPrimitiveDataType(name));
            return getRecursiveSubTypes(name, output);
        }
        return output;
    }

    public HashMap<String, ModelAttribute> getAllAttributesOfModelElement(ModelElement modelElement,
                                                                          HashMap<String, ModelAttribute> output) {
        if (output == null) {
            output = new HashMap<>(modelElement.attributes);
        }
        if (modelElement.getClass() == InheretedClass.class && ((InheretedClass) modelElement).getSuperClass() != null) {
            ModelElement m = getNamedElement(((InheretedClass) modelElement).getSuperClass());
            output.putAll(m.attributes);
            return getAllAttributesOfModelElement(m, output);
        } else if (!modelElement.dependency.isEmpty() && getElement(modelElement.dependency.get(0)) != null && !getElement(modelElement.dependency.get(0)).isMetaClass) {
            ModelElement m = getElement(modelElement.dependency.get(0));
            output.putAll(m.attributes);
            return getAllAttributesOfModelElement(m, output);
        } else {
            return output;
        }
    }

    public String findGenericClassByName(String type) {
        for (InheretedClass ic : inheretedClasses) {
            if (type.contains(ic.name.split("<")[0])) {
                return ic.name.split("<")[0];
            }
        }
        for (ModelElement xd : xmlData.elements.values()) {
            if (type.contains(xd.name.split("<")[0])) {
                return xd.name.split("<")[0];
            }
        }
        return null;
    }

    public boolean extendsSimObj(ModelElement instanceOf) {
        if (instanceOf.name.equals("SimulationObject")) {
            return true;
        } else if (instanceOf.getClass() == InheretedClass.class && ((InheretedClass) instanceOf).getSuperClass() != null) {
            if (((InheretedClass) instanceOf).getSuperClass().equals("SimulationObject")) {
                return true;
            } else {
                return extendsSimObj(this.getNamedElement(((InheretedClass) instanceOf).getSuperClass()));
            }
        } else if (!instanceOf.dependency.isEmpty() && getElement(instanceOf.dependency.get(0)) != null) {
            if (getElement(instanceOf.dependency.get(0)).name.equals("SimulationObject")) {
                return true;
            } else {
                return extendsSimObj(getElement(instanceOf.dependency.get(0)));
            }
        }
        return false;
    }

    public XMLData getXmlData() {
        return xmlData;
    }

    public ArrayList<InheretedClass> getInheretedClasses() {
        return inheretedClasses;
    }

    public void removeInstance(InstanciatedClass instance) {
        this.instanciatedClasses.remove(instance);
    }

    public void removeModelElement(ModelElement modelElement) {
        if (modelElement.getClass() == InheretedClass.class) {
            this.inheretedClasses.remove(modelElement);
        }
    }

    public ArrayList<String> getAllInstancesOfClass(ModelElement me, String genericType) {
        ArrayList<String> output = new ArrayList<>();
        for (InstanciatedClass instanciatedClass : instanciatedClasses) {
            if ((instanciatedClass.getInstanceOf().name.equals(me.name) && (genericType.isEmpty() || instanciatedClass.getSubTypes().contains(genericType))) || childOf(instanciatedClass.getInstanceOf(), me)) {
                output.add(instanciatedClass.getName());
            }
        }
        return output;
    }

    private boolean childOf(ModelElement potentialChild, ModelElement potentialParent) {
        if (!(potentialChild.getClass() == InheretedClass.class)) {
            return false;
        }
        InheretedClass inheretedClass = (InheretedClass) potentialChild;
        ModelElement nextUp = ElementHandler.elementHandler.getNamedElement(inheretedClass.name);
        while (nextUp != null) {
            if (nextUp == potentialParent) {
                return true;
            } else if (nextUp.getClass() == InheretedClass.class) {
                nextUp = elementHandler.getNamedElement(((InheretedClass) nextUp).getSuperClass());
            } else if (nextUp.getClass() == ModelElement.class && !nextUp.dependency.isEmpty()) {
                nextUp = elementHandler.getElement(nextUp.dependency.get(0));
            } else {
                break;
            }
        }
        return false;
    }

    public InstanciatedClass getInstanceByName(String name) {
        for (InstanciatedClass instanciatedClass : instanciatedClasses) {
            if (instanciatedClass.getName().equals(name)) {
                return instanciatedClass;
            }
        }
        return null;
    }

    public ArrayList<String> getAllInstanceNames() {
        ArrayList<String> output = new ArrayList<>();
        instanciatedClasses.forEach(t -> output.add(t.getName()));
        return output;
    }

    public void addEnum(ModelEnum modelEnum) {
        this.xmlData.elements.put(modelEnum.id, modelEnum);
    }

    public boolean nameExists(String name) {
        for (ModelElement me : xmlData.elements.values()) {
            if (me.name.equals(name)) {
                return true;
            }
        }
        for (ModelElement me : inheretedClasses) {
            if (me.name.equals(name)) {
                return true;
            }
        }
        for (InstanciatedClass me : instanciatedClasses) {
            if (me.getName().equals(name)) {
                return true;
            }
        }
        for (Tab tab : tabs) {
            if (tab.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * adds a new elementPanel
     *
     * @param me the ModelElementPanel
     */
    public void addElement(ModelElementPanel me) {
        if(me.getModelElementType() == ModelElementType.EXTENSION) {
            addExtension((InheretedClass) me.getModelElement());
        } else if(me.getModelElementType() == ModelElementType.INSTANCE) {
            addInstance(me.getInstance());
        } else if(me.getModelElementType() == ModelElementType.ENUM) {
            addEnum((ModelEnum) me.getModelElement());
        }
    }

    /**
     * removes an elementPanel
     *
     * @param name name of the panel to remove
     */
    public void removeElement(String name) {
        inheretedClasses.removeIf(me -> me.name.equals(name));
        instanciatedClasses.removeIf(me -> me.getName().equals(name));
        for (Map.Entry<String, ModelElement> me : xmlData.elements.entrySet()) {
            if (me.getValue().name.equals(name)) {
                xmlData.elements.remove(me.getKey());
            }
        }
    }

    public LinkedHashMap<String, Object> getScenarioConfig() {
        return this.scenarioConfig;
    }

    public void setScenarioConfig(LinkedHashMap<String, Object> scenarioConfig) {
        this.scenarioConfig = scenarioConfig;
    }
}
