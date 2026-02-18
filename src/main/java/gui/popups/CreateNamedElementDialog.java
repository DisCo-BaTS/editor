package gui.popups;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.AttributeTableModel;
import gui.ButtonRendererEditor;
import gui.TypeSelectionComboBox;
import handlers.ElementHandler;
import handlers.Logger;
import model.ModelAttribute;
import model.ModelElement;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;

public class CreateNamedElementDialog extends JDialog {
    private final String elementString;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField enterNameTextField;
    private JLabel enterNameLabel;
    private JRadioButton instanceCreationRadioButton;
    private JRadioButton extensionCreationRadioButton;
    private JComboBox genericTypeTextField;
    private JTextField extensionGenericTextField;
    private JPanel propertiesPanel;
    private final String defaultGenericTypeString = "Enter generic type...";
    private final String defaultextGenericTypeString = "Enter placeholders for generics (e.g.: T,N,M,...)";

    private String result = null;
    private boolean extend = false;
    private String genericType = "";
    private ArrayList<String> allListItems;

    private boolean nameOK;
    private HashMap<String, Object> subDialogResults;
    private String tableModelGeneric = "Choose a generic type above";
    private final HashMap<Integer, ModelAttribute> modelAttributes = new HashMap<>();

    public HashMap<String, Object> getSubDialogResults() {
        return subDialogResults;
    }

    public String getGenericType() {
        return genericType;
    }

    public String getResult() {
        return result;
    }

    public boolean isExtend() {
        return extend;
    }

    public CreateNamedElementDialog(String elementString, boolean isGeneric, int numberOfGenerics, boolean isInstance, String selectedGenericType) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.elementString = elementString;
        ModelElement modelElement = ElementHandler.getInstance().getNamedElement(elementString);

        AttributeTableModel tableModel = null;
        if(!elementString.equals("Enum")) {
            tableModel = new AttributeTableModel(modelElement, selectedGenericType, isInstance);
            JTable attributeTable = new JTable(tableModel);
            attributeTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRendererEditor(tableModel, attributeTable, this));
            attributeTable.getColumnModel().getColumn(2).setCellEditor(new ButtonRendererEditor(tableModel, attributeTable, this));

            attributeTable.setPreferredScrollableViewportSize(new Dimension(400, 150));
            JScrollPane scrollPane = new JScrollPane(attributeTable);
            propertiesPanel.removeAll();
            propertiesPanel.setLayout(new BorderLayout());
            propertiesPanel.add(scrollPane, BorderLayout.CENTER);
        }


        enterNameTextField.setText(elementString.replace("<", "").replace(">", ""));
        enterNameLabel.setText("Enter the name for the newly created object:");
        instanceCreationRadioButton.setText("Create instance of " + elementString);
        extensionCreationRadioButton.setText("Create extension of " + elementString);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        enterNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            Border defaultBorder = enterNameTextField.getBorder();

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
                nameOK = false;
                if (enterNameTextField.getText() != null && !Objects.equals(enterNameTextField.getText(), "")
                        && !enterNameTextField.getText().replace(" ", "").equals("")
                        && !(enterNameTextField.getText().charAt(0) >= 48 && enterNameTextField.getText().charAt(0) <= 57)) {

                    for (int i = 0; i < enterNameTextField.getText().length(); i++) {
                        char c = enterNameTextField.getText().charAt(i);
                        if (c < 48 || (c > 57 && c < 65) || (c > 90 && c < 97) || c > 122) {
                            showTooltip("Name contains invalid characters. Only letters and digits are allowed.");
                            return;
                        }
                    }
                    if (!ElementHandler.getInstance().nameExists(enterNameTextField.getText())) {
                        enterNameTextField.setBorder(defaultBorder);
                        enterNameTextField.setToolTipText(null);
                        buttonOK.setEnabled(true);
                        nameOK = true;
                    } else {
                        showTooltip("Name already exists. Choose another name.");
                        Logger.getInstance().writeWarning("Element name already exists");
                    }
                } else {
                    showTooltip("Name cannot be empty, start with a number, or contain only whitespace.");
                }
            }

            private void showTooltip(String message) {
                enterNameTextField.setBorder(BorderFactory.createLineBorder(Color.RED));
                enterNameTextField.setToolTipText(message);
            }
        });

        AttributeTableModel finalTableModel = tableModel;
        ((JTextField) genericTypeTextField.getEditor().getEditorComponent()).getDocument().addDocumentListener(new DocumentListener() {

            private JTextField jTextField = ((JTextField) genericTypeTextField.getEditor().getEditorComponent());

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

                if (jTextField.getText() != null && !Objects.equals(jTextField.getText(), "")
                        && !jTextField.getText().replace(" ", "").isEmpty()
                        && !(jTextField.getText().charAt(0) >= 48 && jTextField.getText().charAt(0) <= 57)) {

                    for (int i = 0; i < jTextField.getText().length(); i++) {
                        char c = jTextField.getText().charAt(i);
                        if (c < 48 || (c > 57 && c < 65) || (c > 90 && c < 97) || c > 122) {
                            if (numberOfGenerics > 1 && c == 44 && jTextField.getText().split(",").length == numberOfGenerics) {
                                continue;
                            }
                            return;
                        }
                    }

                    if(!modelElement.id.equals("ENUMDUMMY")) {
                        for (int i = 0; i < finalTableModel.getRowCount(); i++) {
                            String currentType = (String) finalTableModel.getValueAt(i, 1);
                            if (currentType.equals(tableModelGeneric)) {
                                finalTableModel.changeValue(jTextField.getText(), i, 1);
                                tableModelGeneric = jTextField.getText();
                            }
                        }
                    }

                    if (nameOK) {
                        buttonOK.setEnabled(true);
                    }
                }
            }
        });

        genericTypeTextField.addFocusListener(new FocusListener() {
            private JTextField jTextField = ((JTextField) genericTypeTextField.getEditor().getEditorComponent());

            public void focusGained(FocusEvent e) {
                if (jTextField.getText().equals(defaultGenericTypeString)) {
                    jTextField.setText("");
                }
            }

            public void focusLost(FocusEvent e) {
                if (jTextField.getText().isEmpty()) {
                    jTextField.setText(defaultGenericTypeString);
                }
            }
        });

        extensionGenericTextField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (extensionGenericTextField.getText().equals(defaultextGenericTypeString)) {
                    extensionGenericTextField.setText("");
                }
            }

            public void focusLost(FocusEvent e) {
                if (extensionGenericTextField.getText().isEmpty()) {
                    extensionGenericTextField.setText(defaultextGenericTypeString);
                }
            }
        });

        extensionCreationRadioButton.addActionListener(e -> {
            if (extensionCreationRadioButton.isSelected()) {
                genericTypeTextField.setVisible(false);
                extensionGenericTextField.setVisible(true);
            } else {
                genericTypeTextField.setVisible(isGeneric);
                extensionGenericTextField.setVisible(false);
            }
            enterNameTextField.requestFocus();
            update();
        });

        instanceCreationRadioButton.addActionListener(e -> {
            if (instanceCreationRadioButton.isSelected()) {
                extensionGenericTextField.setVisible(false);
                genericTypeTextField.setVisible(isGeneric);
            }
            enterNameTextField.requestFocus();
            update();
        });

        this.setTitle("Create element");
        this.genericTypeTextField.setVisible(isGeneric);
        ((JTextField) genericTypeTextField.getEditor().getEditorComponent()).setText(defaultGenericTypeString);
        this.extensionGenericTextField.setText(defaultextGenericTypeString);
        extensionGenericTextField.setVisible(false);
        this.genericTypeTextField.setEditable(true);

        TypeSelectionComboBox typeSelectionComboBox = getTypeSelectionComboBox(genericTypeTextField);
        genericTypeTextField.getEditor().getEditorComponent().addKeyListener(typeSelectionComboBox.getKa());
        if(isInstance) {
            instanceCreationRadioButton.setSelected(true);
            extensionCreationRadioButton.setVisible(false);
            extensionCreationRadioButton.setEnabled(false);
            this.genericType = selectedGenericType;
        }

        this.setResizable(false);
    }

    public CreateNamedElementDialog(String elementName, String modelElementName, boolean isInstance, boolean isGeneric) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        if(modelElementName == null) {
            modelElementName = "enumeration";
        }
        elementString = modelElementName;
        if(isInstance) {
            instanceCreationRadioButton.setSelected(true);
        } else {
            extensionCreationRadioButton.setSelected(true);
        }
        enterNameTextField.setText(elementName);
        if(isGeneric) {
            genericTypeTextField.setVisible(true);
            ((JTextField) genericTypeTextField.getEditor().getEditorComponent()).setText(elementString);
        }
        SwingUtilities.invokeLater(this::onOK);
    }

    private void onOK() {
        this.result = enterNameTextField.getText();
        this.extend = this.extensionCreationRadioButton.isSelected();

        if (!((JTextField) genericTypeTextField.getEditor().getEditorComponent()).getText().equals(defaultGenericTypeString)
                && this.genericTypeTextField.isVisible()) {
            this.genericType = ((JTextField) genericTypeTextField.getEditor().getEditorComponent()).getText();
        } else if (!this.extensionGenericTextField.getText().equals(defaultextGenericTypeString)) {
            this.genericType = this.extensionGenericTextField.getText();
        }

        if (elementString.equals("Identifier")) {
            BehaviorPickerDialog dialog = new BehaviorPickerDialog();
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            if (dialog.isResult()) {
                this.subDialogResults = dialog.getSubDialogResults();
            }
        }

        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
    }

    public void setEnumMode() {
        instanceCreationRadioButton.setEnabled(false);
        extensionCreationRadioButton.setEnabled(false);
    }

    public void update() {
        Dimension dimension = new Dimension(320, this.getPreferredSize().height);
        this.setSize(dimension);

        revalidate();
        repaint();
    }

    TypeSelectionComboBox getTypeSelectionComboBox(JComboBox comboBox) {
        allListItems = new ArrayList<>();
        ElementHandler.getInstance().getAllModelElementNames().forEach(t -> allListItems.add(t));
        TypeSelectionComboBox.defaultDataTypes.forEach(t -> allListItems.add(t));

        Vector<String> v = new Vector<String>();
        v.addElement("");
        ElementHandler.getInstance().getAllModelElementNames().forEach(t -> v.addElement(t));
        TypeSelectionComboBox.defaultDataTypes.forEach(t -> v.addElement(t));

        TypeSelectionComboBox newTypeSelectionCB = new TypeSelectionComboBox(comboBox, allListItems, v);
        newTypeSelectionCB.setModel(new DefaultComboBoxModel(v), "");

        return newTypeSelectionCB;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setEnabled(false);
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        enterNameLabel = new JLabel();
        enterNameLabel.setText("Label");
        panel3.add(enterNameLabel, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enterNameTextField = new JTextField();
        panel3.add(enterNameTextField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        instanceCreationRadioButton = new JRadioButton();
        instanceCreationRadioButton.setSelected(true);
        instanceCreationRadioButton.setText("");
        panel4.add(instanceCreationRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        extensionCreationRadioButton = new JRadioButton();
        extensionCreationRadioButton.setText("");
        panel4.add(extensionCreationRadioButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        genericTypeTextField = new JComboBox();
        panel4.add(genericTypeTextField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        extensionGenericTextField = new JTextField();
        panel4.add(extensionGenericTextField, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(instanceCreationRadioButton);
        buttonGroup.add(extensionCreationRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    public void addNewAttribute(int row, String name, String attribute, String result) {
        ModelAttribute modelAttribute = new ModelAttribute();
        modelAttribute.name = name;
        modelAttribute.type = attribute;
        modelAttribute.setDefaultValue(result);
        modelAttributes.put(row, modelAttribute);
    }

    public HashMap<Integer, ModelAttribute> getNewAttributes() {
        return modelAttributes;
    }
}
