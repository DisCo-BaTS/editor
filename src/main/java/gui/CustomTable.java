package gui;

import gui.modelelements.ModelElementPanel;
import handlers.ElementHandler;
import model.EnumLiteral;
import model.ModelAttribute;
import model.ModelEnum;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class CustomTable extends JTable {
    private ModelElementPanel modelElementPanel;
    private HashMap<String, TypeSelectionComboBox> extensionComboBoxes;

    public void setModelElementPanel(ModelElementPanel modelElementPanel) {
        this.modelElementPanel = modelElementPanel;
    }

    public CustomTable() {
        this.extensionComboBoxes = new HashMap<>();
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION && useExtendedCell(row) && column == 1) {
            TypeSelectionComboBox typeSelectionComboBox;
            if (extensionComboBoxes.containsKey(getValueAt(row, 0).toString())) {
                typeSelectionComboBox = extensionComboBoxes.get(getValueAt(row, 0).toString());
                typeSelectionComboBox.updateModel();
            } else {
                typeSelectionComboBox = createExtendedClassComboBox(null);
                extensionComboBoxes.put(getValueAt(row, 0).toString(), typeSelectionComboBox);
            }
            return new DefaultCellEditor(typeSelectionComboBox.getTypePropertyTextField());
        } else if ((modelElementPanel.getModelElementType() == ModelElementType.INSTANCE && column == 2)
                || (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION /*&& useExtendedCell(row)*/ && column == 2)) {
            ModelAttribute modelAttribute = modelElementPanel.getModelElement().getNamedAttributeIncludingSuper(getValueAt(row, 0).toString());
            if (modelAttribute != null && modelAttribute.type != null && ElementHandler.getInstance().getElement(modelAttribute.type) != null && ElementHandler.getInstance().getElement(modelAttribute.type).getClass() == ModelEnum.class) {
                ArrayList<EnumLiteral> enumLiterals = ((ModelEnum) ElementHandler.getInstance().getElement(modelAttribute.type)).enumLiterals;
                ArrayList<String> enumLiterals2 = new ArrayList<>();
                enumLiterals.forEach(t -> enumLiterals2.add(t.toString()));
                TypeSelectionComboBox typeSelectionComboBox = createExtendedClassComboBox(enumLiterals2);
                return new DefaultCellEditor(typeSelectionComboBox.getTypePropertyTextField());
            }

            ArrayList<String> instances;
            if (modelAttribute != null && modelAttribute.type != null && ElementHandler.getInstance().getElement(modelAttribute.type) != null) {
                String withoutPrefix = modelAttribute.type.substring(modelAttribute.type.indexOf("_") + 1);
                int nextUnderscoreIndex = withoutPrefix.indexOf("_");
                String genericType = "";
                if (nextUnderscoreIndex != -1) {
                    genericType = withoutPrefix.substring(nextUnderscoreIndex + 1).split("_")[0];
                }
                if(genericType.matches(".*\\d.*")) {
                    genericType = "";
                }
                instances = ElementHandler.getInstance().getAllInstancesOfClass(ElementHandler.getInstance().getElement(modelAttribute.type), genericType);
            } else if (ElementHandler.getInstance().getNamedElement(getValueAt(row, 1).toString()) != null) {
                instances = ElementHandler.getInstance().getAllInstancesOfClass(ElementHandler.getInstance().getNamedElement(getValueAt(row, 1).toString()), "");
            } else if (modelAttribute != null && modelAttribute.type != null && TypeSelectionComboBox.defaultDataTypes2.contains(modelAttribute.type.split("_")[1])) {
                instances = new ArrayList<>();
            } else {
                instances = ElementHandler.getInstance().getAllInstanceNames();
            }

            TypeSelectionComboBox typeSelectionComboBox = createExtendedClassComboBox(instances);
            return new DefaultCellEditor(typeSelectionComboBox.getTypePropertyTextField());
        } else {
            return super.getCellEditor(row, column);
        }
    }

    private boolean useExtendedCell(int row) {
        ArrayList<String> attributes = new ArrayList<>();
        modelElementPanel.getModelElement().attributes.values().stream().forEach(t -> attributes.add(t.name));
        return attributes.contains(getValueAt(row, 0).toString());
    }

    public TypeSelectionComboBox createExtendedClassComboBox(ArrayList<String> allListItems) {
        if (allListItems == null) {
            allListItems = new ArrayList<>();
            ArrayList<String> finalAllListItems = allListItems;
            ElementHandler.getInstance().getAllModelElementNames().forEach(t -> finalAllListItems.add(t));
            allListItems.addAll(TypeSelectionComboBox.defaultDataTypes2);
        }

        Vector<String> v = new Vector<>();
        allListItems.forEach(t -> v.addElement(t));

        JComboBox jComboBox = new JComboBox();
        jComboBox.setEditable(true);

        TypeSelectionComboBox typeSelectionComboBox = new TypeSelectionComboBox(jComboBox, allListItems, v);
        typeSelectionComboBox.setModel(new DefaultComboBoxModel(v), "");

        return typeSelectionComboBox;
    }
}


