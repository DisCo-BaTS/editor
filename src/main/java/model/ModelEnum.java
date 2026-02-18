package model;

import java.io.Serializable;
import java.util.ArrayList;

public class ModelEnum extends ModelElement implements Serializable {

    public ArrayList<EnumLiteral> enumLiterals;
    private boolean isAddedEnum;

    public ModelEnum() {
        super();
        enumLiterals = new ArrayList<>();
        isAddedEnum = false;
    }

    public boolean isAddedEnum() {
        return isAddedEnum;
    }

    public void setAddedEnum(boolean addedEnum) {
        isAddedEnum = addedEnum;
    }
}
