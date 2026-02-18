package model;

import java.io.Serializable;

public class EnumLiteral implements Serializable {

    private String literal;

    public EnumLiteral(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    @Override
    public String toString() {
        return literal;
    }
}
