package gui.popups;

import gui.MTSEditorGUI;
import handlers.TemplateHandler;
import handlers.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

/**
 * manages the dialog to create a template with a chosen available name
 */
public class CreateNamedTemplateDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField;
    private boolean nameOK;
    private final MTSEditorGUI mtsEditorGUI;

    public CreateNamedTemplateDialog(MTSEditorGUI mtsEditorGUI) {
        this.mtsEditorGUI = mtsEditorGUI;
        initDialog();
        textField.getDocument().addDocumentListener(new DocumentListener() {
            final Border defaultBorder = textField.getBorder();

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
                if (textField.getText() != null && !Objects.equals(textField.getText(), "")
                        && !textField.getText().replace(" ", "").isEmpty()
                        && !(textField.getText().charAt(0) >= 48 && textField.getText().charAt(0) <= 57)) {

                    for (int i = 0; i < textField.getText().length(); i++) {
                        char c = textField.getText().charAt(i);
                        if (c < 48 || (c > 57 && c < 65) || (c > 90 && c < 97) || c > 122) {
                            showTooltip("Name contains invalid characters. Only letters and digits are allowed.");
                            return;
                        }
                    }
                    if (!TemplateHandler.doesTemplateExist(textField.getText())) {
                        textField.setBorder(defaultBorder);
                        textField.setToolTipText(null);
                        buttonOK.setEnabled(true);
                        nameOK = true;
                    } else {
                        showTooltip("Name already exists. Choose another name.");
                        Logger.getInstance().writeWarning("Name already exists");
                    }
                } else {
                    showTooltip("Name cannot be empty, start with a number, or contain only whitespace.");
                }
                if (nameOK) {
                    buttonOK.setEnabled(true);
                }
            }

            private void showTooltip(String message) {
                textField.setBorder(BorderFactory.createLineBorder(Color.RED));
                textField.setToolTipText(message);
            }
        });
    }

    private void initDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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
        TemplateHandler.saveTemplate(mtsEditorGUI, textField.getText());
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
