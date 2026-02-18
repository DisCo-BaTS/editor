package model;


import handlers.ElementHandler;

import java.io.Serializable;
import java.util.HashMap;

public class InheretedClass extends ModelElement implements Serializable {

    private String superClass;
    public HashMap<String, Object> attributeDefaultValues;

    public HashMap<String, Object> getAttributeDefaultValues() {
        return attributeDefaultValues;
    }

    public InheretedClass() {
        attributeDefaultValues = new HashMap<>();
    }

    public InheretedClass(InheretedClass inheretedClass) {
        this.name = inheretedClass.name;
        this.color = inheretedClass.color;
        this.superClass = inheretedClass.superClass;
        this.attributeDefaultValues = inheretedClass.attributeDefaultValues;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public void setDefaultValue(String attributeName, String newDefaultValue) {
        ModelAttribute modelAttribute = getNamedAttributeIncludingSuper(attributeName);
        attributeDefaultValues.put(modelAttribute.id, newDefaultValue);
    }

    @Override
    public Object getDefaultValue(String attributeName) {
        ModelAttribute modelAttribute = getNamedAttributeIncludingSuper(attributeName);

        if (attributeDefaultValues.containsKey(modelAttribute.id)) {
            return attributeDefaultValues.get(modelAttribute.id);
        }

        ElementHandler elementHandler = ElementHandler.getInstance();
        ModelElement nextUp = elementHandler.getNamedElement(superClass);
        while (nextUp != null) {
            if (nextUp.getClass() == InheretedClass.class) {
                if (((InheretedClass) nextUp).getAttributeDefaultValues().containsKey(attributeName)) {
                    return ((InheretedClass) nextUp).getAttributeDefaultValues().get(attributeName);
                } else {
                    nextUp = elementHandler.getNamedElement(((InheretedClass) nextUp).getSuperClass());
                }
            } else {
                break;
            }
        }

        return getNamedAttributeIncludingSuper(attributeName).getDefaultValue();
    }
}