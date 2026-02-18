package gui.popups;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import library.model.simulation.Behaviour;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class BehaviorPickerDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTree behaviourSelectionTree;
    private JComboBox<methodBoxContent> methodSelectionBox;

    private boolean result = false;
    private String clazz;
    private String method;

    private HashMap<String, Object> subDialogResults = new HashMap<>();

    public HashMap<String, Object> getSubDialogResults() {
        return subDialogResults;
    }

    public boolean isResult() {
        return result;
    }

    public String getClazz() {
        return clazz;
    }

    public String getMethod() {
        return method;
    }

    public BehaviorPickerDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        behaviourSelectionTree.setModel(getTreeModel());
        for (int i = 0; i < behaviourSelectionTree.getRowCount(); i++) {
            behaviourSelectionTree.expandRow(i);
        }

        behaviourSelectionTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)
                    behaviourSelectionTree.getLastSelectedPathComponent();
            methodBoxContent preselection = null;
            if (selectedNode.getUserObject().getClass() == NodeContent.class) {
                NodeContent content = (NodeContent) selectedNode.getUserObject();
                clazz = content.clazz().getName();
                Method[] methods = content.clazz.getMethods();
                DefaultComboBoxModel<methodBoxContent> defaultComboBoxModel = new DefaultComboBoxModel<>();
                boolean found = false;
                ArrayList<methodBoxContent> sortedOut = new ArrayList<>();
                for (Method m : methods) {
                    StringBuilder signature = new StringBuilder(m.getReturnType().getName()
                            .split("\\.")[m.getReturnType().getName().split("\\.").length - 1]);
                    signature.append(" ");
                    StringBuilder methodNameAndReturn = new StringBuilder(m.getName()).append("(");
                    for (Class o : m.getParameterTypes()) {
                        methodNameAndReturn.append(o.getName().split("\\.")[o.getName()
                                .split("\\.").length - 1]).append(", ");
                    }
                    methodNameAndReturn.append(")");
                    String tmp = methodNameAndReturn.toString().replace(", )", ")");
                    signature.append(tmp);
                    methodBoxContent methodBoxContent = new methodBoxContent(signature.toString(), tmp);
                    if (m.getDeclaringClass() == content.clazz) {
                        if (signature.toString().equals("Map nextStep(double)")) {
                            preselection = methodBoxContent;
                            found = true;
                        }
                        defaultComboBoxModel.addElement(methodBoxContent);
                    } else if (signature.toString().equals("Map nextStep(double)")) {
                        sortedOut.add(methodBoxContent);
                    }
                }
                if (!found && !sortedOut.isEmpty()) {
                    preselection = sortedOut.get(0);
                    defaultComboBoxModel.addElement(sortedOut.get(0));
                }
                methodSelectionBox.setModel(defaultComboBoxModel);
                if (preselection != null) {
                    methodSelectionBox.setSelectedItem(preselection);
                }

                if (defaultComboBoxModel.getSize() != 0) {
                    method = ((methodBoxContent) Objects.requireNonNull(methodSelectionBox.getSelectedItem())).nameAndReturn;
                    buttonOK.setEnabled(true);
                }
            } else {
                buttonOK.setEnabled(false);
                methodSelectionBox.setModel(new DefaultComboBoxModel<>());
            }
        });

        methodSelectionBox.addActionListener(e -> method = ((methodBoxContent) Objects.requireNonNull(methodSelectionBox.getSelectedItem())).nameAndReturn);

        this.setTitle("Choose behaviour");
        this.buttonOK.setEnabled(false);
    }

    record NodeContent(Class clazz, String name) {
        public String toString() {
            return name;
        }
    }

    record methodBoxContent(String signature, String nameAndReturn) {
        public String toString() {
            return signature;
        }
    }

    private DefaultTreeModel getTreeModel() {
        java.util.List<Class<Behaviour>> clazzes;
        try (ScanResult scanResult = new ClassGraph().acceptPackages("library.model")
                .enableClassInfo().scan()) {
            clazzes = scanResult
                    .getSubclasses(Behaviour.class.getName())
                    .loadClasses(Behaviour.class);
        }
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Library");
        DefaultTreeModel defaultTreeModel = new DefaultTreeModel(rootNode);
        HashMap<String, DefaultMutableTreeNode> packageNodes = new HashMap<>();
        for (Class<Behaviour> behaviourClass : clazzes) {
            String packageName = behaviourClass.getPackageName().replace("library.model.", "")
                    .split("\\.")[0];
            if (!packageNodes.containsKey(packageName)) {
                packageNodes.put(packageName, new DefaultMutableTreeNode(packageName));
            }
            DefaultMutableTreeNode packageTreeNode = packageNodes.get(packageName);
            packageTreeNode.add(new DefaultMutableTreeNode(new NodeContent(behaviourClass, behaviourClass.getName()
                    .split("\\.")[behaviourClass.getName().split("\\.").length - 1])));
        }
        packageNodes.values().forEach(rootNode::add);

        return defaultTreeModel;
    }

    private void onOK() {
        // add your code here
        this.result = true;
        this.subDialogResults.put("clazz", this.clazz);
        this.subDialogResults.put("method", this.method);
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
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
        contentPane.setPreferredSize(new Dimension(300, 400));
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
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        methodSelectionBox = new JComboBox();
        panel3.add(methodSelectionBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        behaviourSelectionTree = new JTree();
        scrollPane1.setViewportView(behaviourSelectionTree);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
