package model;


import handlers.ElementHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InstanciatedClass implements Serializable {

    private String name;
    private ArrayList<String> subTypes;
    private HashMap<String, Object> attributeValues;
    private ModelElement instanceOf;

    public InstanciatedClass(ModelElement instanceOf) {
        this.instanceOf = instanceOf;
        attributeValues = new HashMap<>();
        subTypes = new ArrayList<>();
    }

    public ModelElement getInstanceOf() {
        return instanceOf;
    }

    public void setValue(String id, Object value, Object oldValue) {
        if (ElementHandler.getInstance().getAllAttributesOfModelElement(instanceOf, null).containsKey(id)) {
            ModelAttribute modelAttribute = ElementHandler.getInstance().getAllAttributesOfModelElement(instanceOf, null).get(id);
            if (modelAttribute.maxMultiplicity == -1) {
                if (!attributeValues.containsKey(id)) {
                    attributeValues.put(id, new ArrayList<>());
                }
                if (oldValue != null && !oldValue.equals("Default Value") && !oldValue.equals("")) {
                    ArrayList attrValue = (ArrayList) this.getNamedAttribute(modelAttribute.name);
                    for (int i = 0; i < attrValue.size(); i++) {
                        if (attrValue.get(i).equals(oldValue)) {
                            attrValue.set(i, value);
                            break;
                        }
                    }
                } else {
                    ((ArrayList) attributeValues.get(id)).add(value);
                }
            } else {
                attributeValues.put(id, value);
            }
        }
    }

    public void setNamedAttribute(String name, Object value, Object oldValue) {
        for (ModelAttribute ma : ElementHandler.getInstance().getAllAttributesOfModelElement(instanceOf, null).values()) {
            if (ma.name.equals(name)) {
                setValue(ma.id, value, oldValue);
            }
        }
    }

    public void finalize() throws Throwable {

    }

    public Object getNamedAttribute(String name) {
        String attributeID = "";
        for (ModelAttribute ma : ElementHandler.getInstance().getAllAttributesOfModelElement(instanceOf, null).values()) {
            if (ma.name.equals(name)) {
                attributeID = ma.id;
            }
        }
        return getAttribute(attributeID);
    }

    public Object getAttribute(String id) {
        if (attributeValues.containsKey(id)) {
            return attributeValues.get(id);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getSubTypes() {
        return subTypes;
    }

    public void setSubTypes(ArrayList<String> subTypes) {
        this.subTypes = subTypes;
    }

    public void addSubType(String subType) {
        this.subTypes.add(subType);
    }

    public String toString() {
        return this.getName();
    }

    public HashMap<String, Object> convertToHashMap() {
        HashMap<String, Object> output = new HashMap<>();
        for (Map.Entry<String, Object> entry : attributeValues.entrySet()) {
            output.put(instanceOf.getAttributeName(entry.getKey()), entry.getValue());
        }
        return output;
    }

    public void removeAttributeValue(String s, Object value) {
        for (ModelAttribute ma : ElementHandler.getInstance().getAllAttributesOfModelElement(instanceOf, null).values()) {
            if (ma.name.equals(s)) {
                if (ma.maxMultiplicity == -1) {
                    if (attributeValues.containsKey(ma.id)) {
                        ArrayList attrValue = (ArrayList) this.getNamedAttribute(s);
                        for (int i = 0; i < attrValue.size(); i++) {
                            if (attrValue.get(i).equals(value)) {
                                attrValue.remove(i);
                                break;
                            }
                        }
                    }
                } else {
                    attributeValues.remove(ma.id);
                }
            }
        }
    }

    public int getAttributeValueSize() {
        return attributeValues.size();
    }
}