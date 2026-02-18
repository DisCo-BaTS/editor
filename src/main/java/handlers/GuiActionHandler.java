package handlers;

import gui.modelelements.ModelElementPanel;
import model.EnumLiteral;
import model.InheretedClass;
import model.InstanciatedClass;
import model.ModelAttribute;
import model.ModelElement;
import model.ModelEnum;

import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class GuiActionHandler {

    private static GuiActionHandler instance;

    public static GuiActionHandler getInstance() {
        if (instance == null) {
            GuiActionHandler.instance = new GuiActionHandler();
        }
        return GuiActionHandler.instance;
    }

    private final ElementHandler elementHandler = ElementHandler.getInstance();

    public InstanciatedClass createNewInstance(String id) {
        InstanciatedClass instanciatedClass = new InstanciatedClass(elementHandler.getElement(id));
        elementHandler.addInstance(instanciatedClass);
        return instanciatedClass;
    }

    public InheretedClass createExtension(String exendedClass, String newName) {
        InheretedClass inheretedClass = elementHandler.extendClass(exendedClass, newName);
        return inheretedClass;
    }

    public String addAttributeToClass(ModelElement modelElement, String name, String type, String defaultValue,
                                      ArrayList<String> chosenGenerics) {
        String newID = ElementHandler.getID();
        ModelAttribute modelAttribute = new ModelAttribute();
        modelAttribute.name = name;
        modelAttribute.type = type;
        modelAttribute.setDefaultValue(defaultValue);
        modelAttribute.minMultiplicity = 0;
        modelAttribute.maxMultiplicity = 1;
        modelAttribute.visibility = "private";
        modelAttribute.id = newID;
        if (chosenGenerics != null) {
            modelAttribute.chosenSubTypes = chosenGenerics;
        }
        modelElement.attributes.put(newID, modelAttribute);
        Logger.getInstance().writeInfo("Attribute " + name + " of type " + type + " was created");
        return modelAttribute.id;
    }

    public boolean changeAttributeName(String currentName, Object aValue, ModelElement modelElement) {
        for (Map.Entry<String, ModelAttribute> entry : modelElement.attributes.entrySet()) {
            if (entry.getValue().name.equals(currentName)) {
                entry.getValue().name = aValue.toString();
                return true;
            }
        }
        return false;
    }

    public boolean changeAttributeType(String name, Object aValue, ModelElement modelElement) {
        for (Map.Entry<String, ModelAttribute> entry : modelElement.attributes.entrySet()) {
            if (entry.getValue().name.equals(name)) {
                entry.getValue().type = aValue.toString();
                return true;
            }
        }
        return false;
    }

    public boolean changeAttributeDefValue(String name, Object aValue, ModelElement modelElement) {
        for (Map.Entry<String, ModelAttribute> entry : modelElement.attributes.entrySet()) {
            if (entry.getValue().name.equals(name)) {
                entry.getValue().setDefaultValue(aValue.toString());
                return true;
            }
        }
        if (modelElement.getClass() == InheretedClass.class) {
            ModelAttribute ma = modelElement.getNamedAttributeIncludingSuper(name);
            ((InheretedClass) modelElement).getAttributeDefaultValues().put(ma.id, aValue);
            return true;
        }

        return false;
    }

    public ModelEnum createNewEnum(String result) {
        ModelEnum modelEnum = new ModelEnum();
        modelEnum.name = result;
        modelEnum.enumLiterals = new ArrayList<>();
        modelEnum.id = ElementHandler.getID();
        elementHandler.addEnum(modelEnum);
        modelEnum.setAddedEnum(true);
        return modelEnum;
    }

    public boolean changeEnumLiteral(ModelElementPanel modelElementPanel1, String current, Object aValue) {
        if (modelElementPanel1.getModelElement().getClass() != ModelEnum.class) {
            return false;
        }
        for (int i = 0; i < ((ModelEnum) modelElementPanel1.getModelElement()).enumLiterals.size(); i++) {
            if (((ModelEnum) modelElementPanel1.getModelElement()).enumLiterals.get(i).toString().equals(current)) {
                ((ModelEnum) modelElementPanel1.getModelElement()).enumLiterals.get(i).setLiteral(aValue.toString());
                return true;
            }
        }
        return false;
    }

    public void addLiteralToEnum(ModelElement modelElement, String literal) {
        if (modelElement.getClass() != ModelEnum.class) {
            return;
        }
        for (EnumLiteral enumLiteral : ((ModelEnum) modelElement).enumLiterals) {
            if (enumLiteral.getLiteral().equals(literal)) {
                return;
            }
        }
        ((ModelEnum) modelElement).enumLiterals.add(new EnumLiteral(literal));
    }

    public void takeScenarioConfig(TableModel tableModel) {
        LinkedHashMap<String, Object> converted = new LinkedHashMap<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            converted.put(tableModel.getValueAt(i, 0).toString(), tableModel.getValueAt(i, 1));
        }
        if (ElementHandler.getInstance().getScenarioConfig() == null) {
            elementHandler.setScenarioConfig(converted);
            return;
        }
        HashMap<String, Object> original = elementHandler.getScenarioConfig();
        for (Map.Entry<String, Object> entry : converted.entrySet()) {
            if (original.containsKey(entry.getKey())) {
                original.replace(entry.getKey(), entry.getValue());
            } else {
                original.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
