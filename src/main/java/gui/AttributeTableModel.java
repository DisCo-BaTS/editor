package gui;

import handlers.ElementHandler;
import model.ModelAttribute;
import model.ModelElement;
import modelspaceinterface.DefaultDataTypes;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Attribute table of a model element, shown in CreateNamedElementDialog
 */
public class AttributeTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Attribute", "Type", "Value"};
    private final Object[][] rowData;
    private static final String CHOOSE_GENERIC_MESSAGE = "Choose a generic type above";
    private ModelElement modelElement;
    private String selectedGenericType;
    private boolean isInstance;

    public AttributeTableModel(ModelElement modelElement, String selectedGenericType, boolean isInstance) {
        this.modelElement = modelElement;
        this.selectedGenericType = selectedGenericType;
        this.isInstance = isInstance;
        ElementHandler eh = ElementHandler.getInstance();
        HashMap<String, ModelAttribute> attr = eh.getAllAttributesOfModelElement(modelElement, null);
        ModelAttribute[] modelAttributes = attr.values().toArray(new ModelAttribute[0]);

        rowData = new Object[attr.size()][3];
        int count = 0;
        for (int i = 0; i < attr.size(); i++) {
            rowData[i - count][0] = modelAttributes[i].name;

            if (eh.getElement(modelAttributes[i].type) != null) {
                ArrayList<String> a = eh.getPartOfModel(modelAttributes[i].type);
                rowData[i - count][1] = eh.getElement(modelAttributes[i].type).name;
                if (a.size() > 1) {
                    rowData[i - count][1] = rowData[i - count][1].toString().split("<")[0];
                    rowData[i - count][1] = rowData[i - count][1].toString() + "<";
                    for (int k = 1; k < a.size(); k++) {
                        rowData[i - count][1] = rowData[i - count][1].toString() + a.get(k) + ",";
                    }
                    rowData[i - count][1] = rowData[i - count][1].toString().substring(0, rowData[i - count][1].toString().length() - 1);
                    rowData[i - count][1] = rowData[i - count][1].toString() + ">";
                }
                if (modelAttributes[i].chosenSubTypes != null && !modelAttributes[i].chosenSubTypes.isEmpty()) {
                    rowData[i - count][1] = rowData[i - count][1].toString().replace(">", "");
                    rowData[i - count][1] = rowData[i - count][1].toString().replace("<", "");
                    rowData[i - count][1] = rowData[i - count][1].toString().substring(0, rowData[i - count][1].toString().length() - 1);
                    for (String s : modelAttributes[i].chosenSubTypes) {
                        rowData[i - count][1] = rowData[i - count][1].toString() + "<" + s.split("<")[0];
                    }
                    for (int k = 0; k < modelAttributes[i].chosenSubTypes.size(); k++) {
                        rowData[i - count][1] = rowData[i - count][1] + ">";
                    }
                }
            } else if (DefaultDataTypes.getPrimitiveDatatype(modelAttributes[i].type) != null) {
                rowData[i - count][1] = DefaultDataTypes.getPrimitiveDatatype(modelAttributes[i].type);
            } else if (DefaultDataTypes.getSpecialDatatype(modelAttributes[i].type) != null) {
                rowData[i - count][1] = DefaultDataTypes.getSpecialDatatype(modelAttributes[i].type);
            }

            rowData[i - count][2] = "Default Value";
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public int getRowCount() {
        return rowData.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (rowData[row][col] == null) {
            if(isInstance) {
                rowData[row][col] = selectedGenericType;
            } else {
                rowData[row][col] = CHOOSE_GENERIC_MESSAGE;
            }
        }
        return rowData[row][col];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 2 && isValidType(aValue, rowData[rowIndex][columnIndex - 1].toString())) {
            rowData[rowIndex][columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public void changeValue(Object aValue, int rowIndex, int columnIndex) {
        rowData[rowIndex][columnIndex] = aValue;
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    private boolean isValidType(Object value, String typeName) {
        try {
            switch (typeName) {
                case "int", "Integer":
                    Integer.parseInt(value.toString());
                    break;
                case "short", "Short":
                    Short.parseShort(value.toString());
                    break;
                case "float", "Float":
                    Float.parseFloat(value.toString());
                    break;
                case "byte", "Byte":
                    Byte.parseByte(value.toString());
                    break;
                case "long", "Long":
                    Long.parseLong(value.toString());
                    break;
                case "double", "Double":
                    Double.parseDouble(value.toString());
                    break;
                case "boolean", "Boolean":
                    if (!value.toString().equalsIgnoreCase("true") && !value.toString().equalsIgnoreCase("false")) {
                        throw new IllegalArgumentException("Invalid boolean value");
                    }
                    break;
                case "String":
                    break;
                default:
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ModelElement getModelElement() {
        return modelElement;
    }

    public static String getChooseGenericMessage() {
        return CHOOSE_GENERIC_MESSAGE;
    }
}