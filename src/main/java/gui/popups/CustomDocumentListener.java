package gui.popups;

import gui.TypeSelectionComboBox;
import handlers.ElementHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Objects;

public class CustomDocumentListener implements DocumentListener {
    JComboBox jComboBox;
    JButton buttonOK;
    Boolean typeOK;
    AddAttributeDialog addAttributeDialog;

    private boolean addedChild = false;

    public CustomDocumentListener(JComboBox jComboBox, JButton buttonOK, Boolean typeOK, AddAttributeDialog addAttributeDialog) {
        this.jComboBox = jComboBox;
        this.buttonOK = buttonOK;
        this.typeOK = typeOK;
        this.addAttributeDialog = addAttributeDialog;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        propertyChange();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        propertyChange();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        propertyChange();
    }

    public void propertyChange() {
        buttonOK.setEnabled(false);
        if (((JTextField) jComboBox.getEditor().getEditorComponent()).getText() != null
                && !Objects.equals(((JTextField) jComboBox.getEditor().getEditorComponent()).getText(), "")
                && !((JTextField) jComboBox.getEditor().getEditorComponent()).getText().replace(" ", "").equals("")
                && !(((JTextField) jComboBox.getEditor().getEditorComponent()).getText().charAt(0)
                >= 48 && ((JTextField) jComboBox.getEditor().getEditorComponent()).getText().charAt(0) <= 57)) {

            for (int i = 0; i < ((JTextField) jComboBox.getEditor().getEditorComponent()).getText().length(); i++) {
                char c = ((JTextField) jComboBox.getEditor().getEditorComponent()).getText().charAt(i);
                if (c < 48 || (c > 57 && c < 65) || (c > 90 && c < 97) || c > 122) {
                    if (c == 44 || c == 60 || c == 62) {
                        continue;
                    }
                    return;
                }
            }
            addAttributeDialog.typeOK = true;
            String defaultTypeString = "Generic Type...";
            ElementHandler elementHandler = ElementHandler.getInstance();
            if (elementHandler.getNamedElement(((JTextField) jComboBox.getEditor().getEditorComponent()).getText()) != null
                    && elementHandler.getNamedElement(((JTextField) jComboBox.getEditor().getEditorComponent()).getText()).hasGenericType
                    && !addedChild) {
                JComboBox newJComboBox = new JComboBox();
                newJComboBox.setEditable(true);
                SwingUtilities.invokeLater(() -> ((JTextField) newJComboBox.getEditor().getEditorComponent()).setText(defaultTypeString));
                TypeSelectionComboBox typeSelectionComboBox = addAttributeDialog.getTypeSelectionComboBox(newJComboBox);
                ((JTextField) newJComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(new CustomDocumentListener(newJComboBox, buttonOK, typeOK, addAttributeDialog));
                newJComboBox.getEditor().getEditorComponent().addKeyListener(typeSelectionComboBox.getKa());

                newJComboBox.setSize(addAttributeDialog.typePropertyTextField.getPreferredSize());


                GridBagConstraints c = new GridBagConstraints();
                c.weightx = 1;
                c.weighty = 1;
                c.fill = GridBagConstraints.BOTH;
                c.gridx = 0;
                c.gridy = addAttributeDialog.lasty + 1;
                c.insets.top = 5;
                addAttributeDialog.lasty++;

                addAttributeDialog.upperPanel.add(newJComboBox, c);
                addAttributeDialog.comboBoxes.put(newJComboBox, addAttributeDialog.getMaxCOmboBoxInt() + 1);
                addAttributeDialog.update();

                newJComboBox.getEditor().getEditorComponent().addFocusListener(new FocusListener() {
                    public void focusGained(FocusEvent e) {
                        if (((JTextField) newJComboBox.getEditor().getEditorComponent()).getText().equals(defaultTypeString)) {
                            ((JTextField) newJComboBox.getEditor().getEditorComponent()).setText("");
                        }
                    }

                    public void focusLost(FocusEvent e) {
                        if (((JTextField) newJComboBox.getEditor().getEditorComponent()).getText().equals("")) {
                            ((JTextField) newJComboBox.getEditor().getEditorComponent()).setText(defaultTypeString);
                        }
                    }
                });

                addedChild = true;
            } else if (addedChild && !(elementHandler.getNamedElement(((JTextField) jComboBox.getEditor().getEditorComponent()).getText()) != null
                    && elementHandler.getNamedElement(((JTextField) jComboBox.getEditor().getEditorComponent()).getText()).hasGenericType)) {
                addAttributeDialog.removeChilds(jComboBox);
                addedChild = false;
            }

            if (addAttributeDialog.nameOK && addAttributeDialog.typeOK) {
                buttonOK.setEnabled(true);
            }
        }
    }


}
