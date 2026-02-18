package model;

import java.io.Serializable;
import java.util.ArrayList;

public class ModelAttribute implements Serializable {

    public String name;
    public short minMultiplicity;
    public short maxMultiplicity;
    public String type;
    private String defaultValue;
    public String visibility;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ArrayList<String> chosenSubTypes = new ArrayList<>();

    public String id;
}
