package gui.popups;

import handlers.ElementHandler;
import handlers.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Objects;

/**
 * manages the dialog shown if a template is loaded but an element is already existing
 */
public class ElementExistsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton changeNameButton;
    private JRadioButton deleteButton;
    private JTextField changeNameTextField;
    private JLabel label;
    private JCheckBox repeatActionForAllElementsCheckBox;
    private boolean result;
    private boolean abort;
    private final Border defaultBorder = changeNameTextField.getBorder();
    private final ArrayList<String> usedNames;

    public ElementExistsDialog(String name, boolean repeatForAllElements, String repeatAction, ArrayList<String> usedNames) {
        initDialog();
        label.setText("Element already exists: " + name);
        changeNameTextField.setText(name);
        this.usedNames = usedNames;

        changeNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkName();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkName();
            }
        });

        if(repeatForAllElements) {
            repeatActionForAllElementsCheckBox.setSelected(true);
            if(repeatAction.equals("delete")) {
                deleteButton.setSelected(true);
            }
            SwingUtilities.invokeLater(this::onOK);
        }
    }

    private void checkName() {
        buttonOK.setEnabled(false);
        if (changeNameTextField.getText() != null && !Objects.equals(changeNameTextField.getText(), "")
                && !changeNameTextField.getText().replace(" ", "").isEmpty()
                && !(changeNameTextField.getText().charAt(0) >= 48 && changeNameTextField.getText().charAt(0) <= 57)
                && !usedNames.contains(changeNameTextField.getText())) {

            for (int i = 0; i < changeNameTextField.getText().length(); i++) {
                char c = changeNameTextField.getText().charAt(i);
                if (c < 48 || (c > 57 && c < 65) || (c > 90 && c < 97) || c > 122) {
                    showTooltip("Name contains invalid characters. Only letters and digits are allowed.");
                    return;
                }
            }
            if (!ElementHandler.getInstance().nameExists(changeNameTextField.getText())) {
                changeNameTextField.setBorder(defaultBorder);
                changeNameTextField.setToolTipText(null);
                buttonOK.setEnabled(true);
            } else {
                showTooltip("Name already exists. Choose another name.");
                Logger.getInstance().writeWarning("Element name already exists");
            }
        } else {
            showTooltip("Name cannot be empty, start with a number, or contain only whitespace.");
        }
    }

    private void showTooltip(String message) {
        changeNameTextField.setBorder(BorderFactory.createLineBorder(Color.RED));
        changeNameTextField.setToolTipText(message);
    }

    private void initDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        ButtonGroup group = new ButtonGroup();
        group.add(changeNameButton);
        group.add(deleteButton);
        ItemListener radioButtonListener = e -> {
            boolean isChangeNameSelected = changeNameButton.isSelected();

            changeNameTextField.setEnabled(isChangeNameSelected);
            changeNameTextField.setEditable(isChangeNameSelected);
            changeNameTextField.setVisible(isChangeNameSelected);

            if(isChangeNameSelected) {
                repeatActionForAllElementsCheckBox.setSelected(false);
            }
            repeatActionForAllElementsCheckBox.setVisible(!isChangeNameSelected);

            boolean isAnyButtonSelected = changeNameButton.isSelected() || deleteButton.isSelected();
            buttonOK.setEnabled(isAnyButtonSelected);
            if(changeNameButton.isSelected()) {
                checkName();
            }

            contentPane.revalidate();
            contentPane.repaint();
        };
        changeNameButton.addItemListener(radioButtonListener);
        deleteButton.addItemListener(radioButtonListener);
        changeNameButton.setSelected(true);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        result = true;
        dispose();
    }

    private void onCancel() {
        abort = true;
        dispose();
    }

    public boolean isRepeatForAllElements() {
        return repeatActionForAllElementsCheckBox.isSelected();
    }

    public boolean isResult() {
        return result;
    }

    public boolean isAbort() {
        return abort;
    }

    public boolean isChangeNameButton() {
        return changeNameButton.isSelected();
    }

    public boolean isDeleteButtonSelected() {
        return deleteButton.isSelected();
    }

    public String getNewName() {
        return changeNameTextField.getText();
    }

    public String getRepeatedAction() {
        String repeatedAction = "change";
        if(deleteButton.isSelected()) {
            repeatedAction = "delete";
        }
        return repeatedAction;
    }
}
