package gui.popups;

import handlers.TemplateHandler;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Objects;

/**
 * manages the dialog which shows a list of all available templates
 */
public class TemplatesListDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<String> templateList;
    private boolean confirmed;
    private String result;

    public TemplatesListDialog() {
        initDialog();
        loadTemplatesFromDirectory();
        templateList.addActionListener(e -> buttonOK.setEnabled(templateList.getSelectedItem() != null && !templateList.getSelectedItem().toString().isEmpty()));
    }

    private void loadTemplatesFromDirectory() {
        File templateDir = new File(TemplateHandler.getTemplateFolder());
        if (templateDir.exists() && templateDir.isDirectory()) {
            DefaultComboBoxModel<String> listModel = new DefaultComboBoxModel<>();
            listModel.addElement("");
            for (File file : Objects.requireNonNull(templateDir.listFiles((dir, name) -> name.endsWith(".tss")))) {
                listModel.addElement(file.getName().replace(".tss", ""));
            }
            templateList.setModel(listModel);
        }
    }

    private void initDialog() {
        setTitle("Available Templates");
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
        this.result = templateList.getSelectedItem().toString();
        this.confirmed = true;
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getResult() {
        return result;
    }
}
