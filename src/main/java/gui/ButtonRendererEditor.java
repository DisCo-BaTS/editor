package gui;

import gui.popups.CreateNamedElementDialog;
import handlers.ElementHandler;
import model.ModelElement;
import modelspaceinterface.DefaultDataTypes;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.EventObject;

/**
 * Creates a button or text field in the attribute table to change the value of the attribute
 */
public class ButtonRendererEditor extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
    private final JButton button;
    private String label;
    private final JTextField textField;
    private final AttributeTableModel tableModel;
    private final CreateNamedElementDialog createNamedElementDialog;

    public ButtonRendererEditor(AttributeTableModel tableModel, JTable table, CreateNamedElementDialog createNamedElementDialog) {
        this.createNamedElementDialog = createNamedElementDialog;
        this.tableModel = tableModel;
        textField = new JTextField();
        button = new JButton();
        button.addActionListener(e -> openCreateNamedElementDialog(table.getSelectedRow(), createNamedElementDialog));
    }

    @Override
    public Object getCellEditorValue() {
        if (button.hasFocus()) {
            return label;
        } else {
            return textField.getText();
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String typeName = table.getValueAt(row, 1).toString();
        if (!DefaultDataTypes.isEditableDatatype(typeName) && !typeName.equals(AttributeTableModel.getChooseGenericMessage())) {
            label = (value == null || value.equals("Default Value")) ? "Create new Element" : value.toString();
            button.setText(label);
            return button;
        } else {
            if(!tableModel.getValueAt(row, 2).toString().equals("Default Value")) {
                createNamedElementDialog.addNewAttribute(row, tableModel.getValueAt(row, 0).toString(), tableModel.getValueAt(row, 1).toString(), tableModel.getValueAt(row, 2).toString());
            }
            textField.setText(value.toString());
            return textField;
        }
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        String typeName = table.getValueAt(row, 1).toString();
        if (!DefaultDataTypes.isEditableDatatype(typeName) && !typeName.equals(AttributeTableModel.getChooseGenericMessage())) {
            label = (value == null || value.equals("Default Value")) ? "Create new Element" : value.toString();
            button.setText(label);
            return button;
        } else {
            if(!tableModel.getValueAt(row, 2).toString().equals("Default Value")) {
                createNamedElementDialog.addNewAttribute(row, tableModel.getValueAt(row, 0).toString(), tableModel.getValueAt(row, 1).toString(), tableModel.getValueAt(row, 2).toString());
            }
            textField.setText(value.toString());
            return textField;
        }
    }

    private void openCreateNamedElementDialog(int row, CreateNamedElementDialog createNamedElementDialog) {
        ModelElement modelElement = ElementHandler.getInstance().getNamedElement(tableModel.getValueAt(row, 1).toString());
        String[] genericType = tableModel.getValueAt(row, 1).toString().split("[<>]");
        String selectGeneric = "";
        if(genericType.length > 1) {
            selectGeneric = genericType[1];
        }
        CreateNamedElementDialog dialog = new CreateNamedElementDialog(tableModel.getValueAt(row, 1).toString(), false, modelElement.subTypes.size(), true, selectGeneric);
        dialog.pack();
        dialog.setLocationRelativeTo(createNamedElementDialog);
        dialog.update();
        dialog.setVisible(true);
        createNamedElementDialog.addNewAttribute(row, tableModel.getValueAt(row, 0).toString(), tableModel.getValueAt(row, 1).toString(), dialog.getResult());
        String createdElement = ListTransferHandler.createModelElementPanel(new ListItem(modelElement.name, modelElement.id),
                null, null, dialog, modelElement.name);
        if(!createdElement.isEmpty()) {
            tableModel.changeValue(createdElement, row, 2);
            stopCellEditing();
        }
    }

    @Override
    public boolean stopCellEditing() {
        return super.stopCellEditing();
    }

    @Override
    public boolean isCellEditable(EventObject e) {
        return true;
    }
}