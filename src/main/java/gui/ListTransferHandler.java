package gui;

import gui.modelelements.AssociationElementPanel;
import gui.modelelements.ModelElementPanel;
import gui.popups.CreateNamedElementDialog;
import handlers.ElementHandler;
import handlers.ElementPanelHandler;
import handlers.GuiActionHandler;
import handlers.Logger;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("serial")
public class ListTransferHandler extends TransferHandler {

    private static int counter = 0;

    private static MTSEditorGUI mtsEditorGUI;
    private final JPanel containerPanel;
    private final ElementPanelHandler elementPanelHandler;

    public ListTransferHandler(MTSEditorGUI mtsEditorGUI, JPanel parent, ElementPanelHandler elementPanelHandler) {
        this.mtsEditorGUI = mtsEditorGUI;
        this.containerPanel = parent;
        this.elementPanelHandler = elementPanelHandler;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return (support.getComponent() instanceof JPanel) && support.isDataFlavorSupported(ListItemTransferable.LIST_ITEM_DATA_FLAVOR);
    }

    @Override
    public boolean importData(TransferSupport support) {
        boolean accept = false;
        if (canImport(support)) {
            try {
                Transferable t = support.getTransferable();
                Object value = t.getTransferData(ListItemTransferable.LIST_ITEM_DATA_FLAVOR);
                if (value instanceof ListItem && support.getComponent() instanceof JPanel) {
                    Point mousePosition = support.getComponent().getMousePosition();
                    importData((ListItem) value, mousePosition, mtsEditorGUI, containerPanel);
                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
        return accept;
    }

    public static boolean importData(ListItem value, Point mousePosition, MTSEditorGUI mtsEditorGUI, JPanel containerPanel) {
        String n = value.getName();
        ModelElement modelElement = ElementHandler.getInstance().getElement(value.getId());
        boolean isInstance = mtsEditorGUI.isUserMode();
        CreateNamedElementDialog dialog = new CreateNamedElementDialog(n, modelElement.hasGenericType, modelElement.subTypes.size(), isInstance, "");
        if (!modelElement.id.equals("ENUMDUMMY") && modelElement.getClass() == ModelEnum.class) {
            Logger.getInstance().writeWarning("Enums can not be instantiated. Choose Enum values in Properties Table");
            return false;
        }
        if (modelElement.id.equals("ENUMDUMMY")) {
            dialog.setEnumMode();
        }
        dialog.pack();
        dialog.setLocationRelativeTo(mtsEditorGUI);
        dialog.update();
        dialog.setVisible(true);

        createModelElementPanel(value, mousePosition, containerPanel, dialog, n);

        return true;
    }

    public static String createModelElementPanel(ListItem value, Point mousePosition, JPanel containerPanel, CreateNamedElementDialog dialog, String n) {
        if(containerPanel == null) {
            containerPanel = (JPanel) mtsEditorGUI.getEditorLayeredPane().getParent();
        }
        if (mousePosition == null) {
            if (counter == 10) {
                counter = 0;
            }
            mousePosition = new Point(50 + (counter * 15), 50 + (counter * 15));
            counter++;
        }
        ElementPanelHandler elementPanelHandler = mtsEditorGUI.getElementPanelHandler();
        ModelElementPanel modelElementPanel = null;
        String otherElementName = null;
        if (dialog.getResult() != null && value.getId().equals("ENUMDUMMY")) {
            ModelEnum modelEnum = GuiActionHandler.getInstance().createNewEnum(dialog.getResult());
            modelElementPanel = new ModelElementPanel(modelEnum.name, null, mtsEditorGUI, containerPanel, mousePosition, ModelElementType.ENUM);
            modelElementPanel.setClass(modelEnum);
            modelElementPanel.setColor(modelEnum.color);
            mtsEditorGUI.elementSelected(modelElementPanel);
        } else if (dialog.getResult() != null && !dialog.isExtend()) {
            InstanciatedClass ic = GuiActionHandler.getInstance().createNewInstance(value.getId());
            ic.setName(dialog.getResult());
            modelElementPanel = new ModelElementPanel(ic.getName(), ic.getInstanceOf().name, mtsEditorGUI, containerPanel, mousePosition, ModelElementType.INSTANCE);
            otherElementName = ic.getInstanceOf().name;
            if (ic.getInstanceOf().hasGenericType && !dialog.getGenericType().equals("")) {
                for (String b : dialog.getGenericType().split(",")) {
                    ic.addSubType(b);
                }
                modelElementPanel.setSubTypes(ic.getSubTypes());
            }
            for(ModelAttribute modelAttribute : dialog.getNewAttributes().values()) {
                ic.setNamedAttribute(modelAttribute.name, modelAttribute.getDefaultValue(), "Default Value");
            }
            modelElementPanel.setInstance(ic);
            if(mtsEditorGUI.getAllInstancesColors().equals(modelElementPanel.getDefaultColor())) {
                modelElementPanel.setColor(ic.getInstanceOf().color);
            } else {
                modelElementPanel.setColor(mtsEditorGUI.getAllInstancesColors());
            }
            mtsEditorGUI.elementSelected(modelElementPanel);
        } else if (dialog.getResult() != null && dialog.isExtend()) {
            InheretedClass nc = GuiActionHandler.getInstance().createExtension(n, dialog.getResult());
            if (!dialog.getGenericType().equals("")) {
                nc.hasGenericType = true;
                nc.subTypes.addAll(Arrays.asList(dialog.getGenericType().split(",")));
                nc.name = nc.name + "<" + dialog.getGenericType() + ">";
            }
            modelElementPanel = new ModelElementPanel(nc.name, n, mtsEditorGUI, containerPanel, mousePosition, ModelElementType.EXTENSION);
            otherElementName = n;
            mtsEditorGUI.addNewExtendedClass(nc);
            modelElementPanel.setClass(nc);
            Color color = ElementHandler.getInstance().getElement(otherElementName).color;
            nc.color = color;
            if(mtsEditorGUI.getAllExtensionsColors().equals(modelElementPanel.getDefaultColor())) {
                modelElementPanel.setColor(color);
            } else {
                modelElementPanel.setColor(mtsEditorGUI.getAllExtensionsColors());
            }
            mtsEditorGUI.elementSelected(modelElementPanel);
        }
        if (dialog.getSubDialogResults() != null && modelElementPanel != null
                && modelElementPanel.getModelElementType() == ModelElementType.INSTANCE
                && modelElementPanel.getInstance() != null) {
            boolean change = false;
            for (Map.Entry<String, Object> entry : dialog.getSubDialogResults().entrySet()) {
                if (modelElementPanel.getModelElement().getNamedAttribute(entry.getKey()) != null) {
                    modelElementPanel.getInstance().setNamedAttribute(entry.getKey(), entry.getValue(), null);
                    change = true;
                }
            }
            if (change) {
                mtsEditorGUI.elementSelected(modelElementPanel);
            }
        }
        if (dialog.getResult() != null) {
            elementPanelHandler.addModelElementPanel(modelElementPanel);
            if(modelElementPanel != null) {
                for(ModelAttribute modelAttribute : dialog.getNewAttributes().values()) {
                    if(elementPanelHandler.getModelElementPanel(modelAttribute.getDefaultValue()) != null) {
                        AssociationElementPanel associationElementPanel = new AssociationElementPanel(modelElementPanel,
                                elementPanelHandler.getModelElementPanel(modelAttribute.getDefaultValue()), mtsEditorGUI);
                        if(dialog.isExtend()) {
                            associationElementPanel.setDefaultValue();
                            elementPanelHandler.getModelElementPanel(modelAttribute.getDefaultValue()).setDefaultValue();
                        }
                        mtsEditorGUI.getEditorPanel().addAssociation(associationElementPanel);
                    }
                }
            }
            mtsEditorGUI.repaint();
            mtsEditorGUI.revalidate();

            if (otherElementName != null && elementPanelHandler.getModelElementPanel(otherElementName) != null) {
                AssociationElementPanel associationElementPanel = new AssociationElementPanel(modelElementPanel,
                        elementPanelHandler.getModelElementPanel(otherElementName), mtsEditorGUI);
                mtsEditorGUI.getEditorPanel().addAssociation(associationElementPanel);
            }
        }
        if(modelElementPanel == null) {
            return "";
        }
        modelElementPanel.setInTab(mtsEditorGUI.getCurrentTab().getName());
        for(Tab tab : ElementHandler.getInstance().getTabs()) {
            if(tab.getName().equals(modelElementPanel.getInTab())) {
                tab.getModelElementPanels().add(modelElementPanel);
            }
        }
        return modelElementPanel.getName();
    }

    @Override
    public int getSourceActions(JComponent c) {
        return DnDConstants.ACTION_COPY_OR_MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        Transferable t = null;
        if (c instanceof JList) {
            @SuppressWarnings("unchecked")
            JList<ListItem> list = (JList<ListItem>) c;
            Object value = list.getSelectedValue();
            if (value instanceof ListItem) {
                ListItem li = (ListItem) value;
                t = new ListItemTransferable(li);
            }
        }
        return t;
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {

    }
}