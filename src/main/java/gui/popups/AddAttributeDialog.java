package gui.popups;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.TypeSelectionComboBox;
import handlers.ElementHandler;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class AddAttributeDialog extends AddDialogs {
    public HashMap<JComboBox, Integer> comboBoxes;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField namePropertyTextField;
    private final String defaultNameString = "Name...";
    JComboBox typePropertyTextField;
    private final String defaultTypeString = "Type...";
    private JTextField defaultValueTextField;
    JPanel upperPanel;
    private JComboBox multiplicitySelection;
    private final String defaultDefaultValueString = "Default Value...";

    Boolean nameOK = false;
    Boolean typeOK = false;
    private boolean result = false;

    private String nameProp = "";
    private String typeProp = "";
    private String defVal = "";

    private ArrayList<String> allListItems;
    private TypeSelectionComboBox typeSelectionComboBox;

    int lasty = 2;

    public boolean isResult() {
        return result;
    }

    public String getNameProp() {
        return nameProp;
    }

    public String getTypeProp() {
        return typeProp;
    }

    public String getDefVal() {
        return defVal;
    }

    TypeSelectionComboBox getTypeSelectionComboBox(JComboBox comboBox) {
        allListItems = new ArrayList<>();
        ElementHandler.getInstance().getAllModelElementNames().forEach(t -> allListItems.add(t));
        TypeSelectionComboBox.defaultDataTypes.forEach(t -> allListItems.add(t));

        Vector<String> v = new Vector<String>();
        ElementHandler.getInstance().getAllModelElementNames().forEach(t -> v.addElement(t));
        TypeSelectionComboBox.defaultDataTypes.forEach(t -> v.addElement(t));

        TypeSelectionComboBox newTypeSelectionCB = new TypeSelectionComboBox(comboBox, allListItems, v);
        newTypeSelectionCB.setModel(new DefaultComboBoxModel(v), "");

        return newTypeSelectionCB;
    }

    void update() {
        this.setSize(this.getPreferredSize());

        revalidate();
        repaint();
    }

    /**
     * https://www.roseindia.net/tutorial/java/swing/autosuggest.html
     */
    public AddAttributeDialog() {
        typeSelectionComboBox = getTypeSelectionComboBox(typePropertyTextField);
        this.comboBoxes = new HashMap<>();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        namePropertyTextField.setText(defaultNameString);
        SwingUtilities.invokeLater(() -> ((JTextField) typePropertyTextField.getEditor().getEditorComponent()).setText(defaultTypeString));
        defaultValueTextField.setText(defaultDefaultValueString);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        namePropertyTextField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (namePropertyTextField.getText().equals(defaultNameString)) {
                    namePropertyTextField.setText("");
                }
            }

            public void focusLost(FocusEvent e) {
                if (namePropertyTextField.getText().equals("")) {
                    namePropertyTextField.setText(defaultNameString);
                }
            }
        });

        typePropertyTextField.getEditor().getEditorComponent().addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (((JTextField) typePropertyTextField.getEditor().getEditorComponent()).getText().equals(defaultTypeString)) {
                    ((JTextField) typePropertyTextField.getEditor().getEditorComponent()).setText("");
                }
            }

            public void focusLost(FocusEvent e) {
                if (((JTextField) typePropertyTextField.getEditor().getEditorComponent()).getText().equals("")) {
                    ((JTextField) typePropertyTextField.getEditor().getEditorComponent()).setText(defaultTypeString);
                }
            }
        });

        defaultValueTextField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (defaultValueTextField.getText().equals(defaultDefaultValueString)) {
                    defaultValueTextField.setText("");
                }
            }

            public void focusLost(FocusEvent e) {
                if (defaultValueTextField.getText().equals("")) {
                    defaultValueTextField.setText(defaultDefaultValueString);
                }
            }
        });

        namePropertyTextField.getDocument().addDocumentListener(new DocumentListener() {
            Border defaultB;

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
                if (namePropertyTextField.getText() != null && !Objects.equals(namePropertyTextField.getText(), "")
                        && !namePropertyTextField.getText().replace(" ", "").equals("")
                        && !(namePropertyTextField.getText().charAt(0) >= 48 && namePropertyTextField.getText().charAt(0) <= 57)) {

                    for (int i = 0; i < namePropertyTextField.getText().length(); i++) {
                        char c = namePropertyTextField.getText().charAt(i);
                        if (c < 48 || (c > 57 && c < 65) || (c > 90 && c < 97) || c > 122) {
                            return;
                        }
                    }
                    nameOK = true;
                    if (typeOK) {
                        buttonOK.setEnabled(true);
                    }
                }
            }
        });

        ((JTextField) typePropertyTextField.getEditor().getEditorComponent()).getDocument().addDocumentListener(new CustomDocumentListener(typePropertyTextField, buttonOK, typeOK, this));
        comboBoxes.put(typePropertyTextField, 0);

        this.buttonOK.setEnabled(false);
        this.setTitle("Create attribute");
        this.setResizable(false);
        typePropertyTextField.setEditable(true);

        typePropertyTextField.getEditor().getEditorComponent().addKeyListener(typeSelectionComboBox.getKa());

        Vector<MultiElement> multi = new Vector<>();
        multi.add(new MultiElement("Single value [1]", false));
        multi.add(new MultiElement("Multiple values [0..*]", true));
        multiplicitySelection.setModel(new DefaultComboBoxModel(multi));
    }

    record MultiElement(String text, boolean multi) {

        public String getText() {
            return text;
        }

        public boolean isMulti() {
            return multi;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private void onOK() {
        if (namePropertyTextField.getText().equals(defaultNameString)) {
            nameOK = false;
            buttonOK.setEnabled(false);
            return;
        }
        this.result = true;
        if (nameOK && !namePropertyTextField.getText().equals(defaultNameString)) {
            this.nameProp = namePropertyTextField.getText();
        }
        if (typeOK && !((JTextField) typePropertyTextField.getEditor().getEditorComponent()).getText().equals(defaultTypeString)) {
            this.typeProp = ((JTextField) typePropertyTextField.getEditor().getEditorComponent()).getText();
        }
        if (!defaultValueTextField.getText().equals(defaultDefaultValueString)) {
            this.defVal = defaultValueTextField.getText();
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public Integer getMaxCOmboBoxInt() {
        int maxInt = Integer.MIN_VALUE;
        for (Integer i : comboBoxes.values()) {
            if (i > maxInt) {
                maxInt = i;
            }
        }
        return maxInt;
    }

    public void removeChilds(JComboBox jComboBox) {
        int i = comboBoxes.get(jComboBox);
        int max = getMaxCOmboBoxInt();

        ArrayList<JComboBox> toRemove = new ArrayList<>();
        for (Map.Entry<JComboBox, Integer> entry : comboBoxes.entrySet()) {
            if (entry.getValue() > i) {
                toRemove.add(entry.getKey());
                upperPanel.remove(entry.getKey());
                lasty--;
            }
        }
        toRemove.forEach(t -> comboBoxes.remove(t));
        update();
    }

    public HashMap<JComboBox, Integer> getComboBoxes() {
        HashMap<JComboBox, Integer> newMap = new HashMap<>(comboBoxes);
        newMap.remove(typePropertyTextField);
        return newMap;
    }

    public boolean getMulti() {
        return ((MultiElement) multiplicitySelection.getSelectedItem()).isMulti();
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
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        upperPanel = new JPanel();
        upperPanel.setLayout(new GridBagLayout());
        panel3.add(upperPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Set properties for new attribute...");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 7, 0);
        upperPanel.add(label1, gbc);
        namePropertyTextField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        upperPanel.add(namePropertyTextField, gbc);
        typePropertyTextField = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        upperPanel.add(typePropertyTextField, gbc);
        defaultValueTextField = new JTextField();
        panel3.add(defaultValueTextField, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        multiplicitySelection = new JComboBox();
        panel3.add(multiplicitySelection, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
