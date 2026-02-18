package model;

import handlers.ElementHandler;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModelElement implements Serializable {
    public String name;
    public String type;
    public ArrayList<String> dependency;
    public String id;
    public HashMap<String, ModelAttribute> attributes;
    public boolean isMetaClass = false;
    public boolean hasGenericType = false;
    public ArrayList<String> subTypes;
    public Color color;

    public ModelElement() {
        dependency = new ArrayList<>();
        attributes = new HashMap<>();
        subTypes = new ArrayList<>();
        color = new Color(50, 50, 51, 255);
    }

    public ModelElement(ModelElement modelElement) {
        this.name = modelElement.name;
        this.type = modelElement.type;
        this.dependency = modelElement.dependency;
        this.id = modelElement.id;
        this.attributes = modelElement.attributes;
        this.isMetaClass = modelElement.isMetaClass;
        this.hasGenericType = modelElement.hasGenericType;
        this.subTypes = modelElement.subTypes;
        this.color = modelElement.color;
    }

    public ModelAttribute getNamedAttribute(String name) {
        for (Map.Entry<String, ModelAttribute> entry : attributes.entrySet()) {
            if (entry.getValue().name.equals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String getAttributeName(String id) {
        for (Map.Entry<String, ModelAttribute> entry : attributes.entrySet()) {
            if (entry.getValue().id.equals(id)) {
                return entry.getValue().name;
            }
        }
        return null;
    }

    public ModelAttribute getNamedAttributeIncludingSuper(String name) {
        HashMap<String, ModelAttribute> allAtr = ElementHandler.getInstance().getAllAttributesOfModelElement(this, null);
        for (Map.Entry<String, ModelAttribute> entry : allAtr.entrySet()) {
            if (entry.getValue().name.equals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Object getDefaultValue(String attributeName) {
        return getNamedAttributeIncludingSuper(attributeName).getDefaultValue();
    }
}
