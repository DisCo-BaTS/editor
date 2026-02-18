package handlers;

import gui.EditorPanel;
import gui.ModelElementType;
import gui.modelelements.AssociationElementPanel;
import gui.modelelements.ModelElementPanel;
import model.InheretedClass;
import model.ModelElement;

import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementPanelHandler {
    private ArrayList<ModelElementPanel> modelElementPanels;
    private ElementHandler elementHandler;
    private EditorPanel editorPanel;

    public ArrayList<ModelElementPanel> getModelElementPanels() {
        return modelElementPanels;
    }

    public ElementPanelHandler(EditorPanel editorPanel) {
        modelElementPanels = new ArrayList<>();
        elementHandler = ElementHandler.getInstance();
        this.editorPanel = editorPanel;
    }

    public void addModelElementPanel(ModelElementPanel modelElementPanel) {
        editorPanel.addElement(modelElementPanel);
        modelElementPanels.add(modelElementPanel);
        editorPanel.getMtsEditorGUI().updateHiddenPanels();
        UndoableEdit edit = new AbstractUndoableEdit() {
            private final ArrayList<AssociationElementPanel> associationElementPanels = new ArrayList<>();

            @Override
            public void undo() throws CannotUndoException {
                if(!modelElementPanel.getName().equals("Main")) {
                    super.undo();
                    editorPanel.removeElement(modelElementPanel);
                    modelElementPanels.remove(modelElementPanel);
                    elementHandler.removeElement(modelElementPanel.getName());
                    if(modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
                        editorPanel.getMtsEditorGUI().removeExtendedClass(modelElementPanel.getName());
                    }
                    if(modelElementPanel.getModelElementType() == ModelElementType.TAB) {
                        removeTabs(modelElementPanel, false);
                    }
                    List<AssociationElementPanel> toRemove = new ArrayList<>();

                    for (AssociationElementPanel associationElementPanel : editorPanel.getAssociationElementPanels()) {
                        if (associationElementPanel.containsModelElement(modelElementPanel)) {
                            associationElementPanels.add(associationElementPanel);
                            toRemove.add(associationElementPanel);
                        }
                    }

                    for (AssociationElementPanel removePanel : toRemove) {
                        editorPanel.removeAssociation(removePanel);
                    }
                }
            }

            @Override
            public void redo() throws CannotRedoException {
                if(!modelElementPanel.getName().equals("Main")) {
                    super.redo();
                    editorPanel.addElement(modelElementPanel);
                    modelElementPanels.add(modelElementPanel);
                    elementHandler.addElement(modelElementPanel);
                    if(modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
                        editorPanel.getMtsEditorGUI().addNewExtendedClass((InheretedClass) modelElementPanel.getModelElement());
                    }
                    List<AssociationElementPanel> toRemove = new ArrayList<>();

                    for (AssociationElementPanel associationElementPanel : associationElementPanels) {
                        if (associationElementPanel.containsModelElement(modelElementPanel)) {
                            toRemove.add(associationElementPanel);
                            AssociationElementPanel a = new AssociationElementPanel(
                                    associationElementPanel.getStartModelElementPanel(),
                                    associationElementPanel.getEndModelElementPanel(),
                                    editorPanel.getMtsEditorGUI()
                            );
                            editorPanel.addAssociation(a);
                        }
                    }

                    associationElementPanels.removeAll(toRemove);
                }
            }
        };
        UndoHandler.getInstance().getUndoableEditSupport().postEdit(edit);
    }

    public boolean removeModelElementPanel(ModelElementPanel modelElementPanel, boolean clearSpace) {
        ArrayList<AssociationElementPanel> associationElementPanels = new ArrayList<>();
        for(AssociationElementPanel panel : modelElementPanel.getAssociationPanels()) {
            if(panel.getEndModelElementPanel().equals(modelElementPanel) && !clearSpace && modelElementPanel.getModelElementType() != ModelElementType.TAB) {
                Logger.getInstance().writeWarning("Element cannot be deleted, because Association exists");
                return false;
            }
            associationElementPanels.add(panel);
        }
        modelElementPanels.remove(modelElementPanel);
        editorPanel.removeElement(modelElementPanel);
        removeTabs(modelElementPanel, clearSpace);
        editorPanel.getMtsEditorGUI().updateHiddenPanels();
        if (modelElementPanel.getModelElementType() == ModelElementType.INSTANCE) {
            elementHandler.removeInstance(modelElementPanel.getInstance());
            Logger.getInstance().writeInfo("Instance of " + modelElementPanel.getClazz() + "(" + modelElementPanel.getName() + ")" + " was " +
                    "deleted");
        } else if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
            Logger.getInstance().writeInfo("Extension of " + modelElementPanel.getClazz() + "(" + modelElementPanel.getName() + ")" + " was " +
                    "deleted");
            if(!modelElementPanels.isEmpty()) {
                for(ModelElementPanel me : modelElementPanels) {
                    if(!((me.getModelElementType() == ModelElementType.INSTANCE && me.getInstance().getInstanceOf().equals(modelElementPanel.getModelElement()))
                            || (me.getModelElementType() == ModelElementType.EXTENSION
                            && ((InheretedClass) me.getModelElement()).getSuperClass().equals(modelElementPanel.getName())))) {
                        elementHandler.removeModelElement(modelElementPanel.getModelElement());
                        break;
                    }
                }
            } else {
                elementHandler.removeModelElement(modelElementPanel.getModelElement());
            }
        }
        UndoableEdit edit = new AbstractUndoableEdit() {
            @Override
            public void undo() throws CannotUndoException {
                super.undo();
                for(AssociationElementPanel panel : associationElementPanels) {
                    editorPanel.addAssociation(panel);
                    modelElementPanel.addAssociationPanel(panel);
                    panel.getEndModelElementPanel().addAssociationPanel(panel);
                }
                editorPanel.addElement(modelElementPanel);
                modelElementPanels.add(modelElementPanel);
                if(!elementHandler.nameExists(modelElementPanel.getName())) {
                    elementHandler.addElement(modelElementPanel);
                }
                if(modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
                    editorPanel.getMtsEditorGUI().addNewExtendedClass((InheretedClass) modelElementPanel.getModelElement());
                }
            }

            @Override
            public void redo() throws CannotRedoException {
                super.redo();
                if (removeModelElementPanel(modelElementPanel, false)) {
                    List<AssociationElementPanel> associationPanelsCopy = new ArrayList<>(modelElementPanel.getAssociationPanels());
                    associationPanelsCopy.forEach(t -> editorPanel.removeAssociation(t));
                }
            }
        };
        UndoHandler.getInstance().getUndoableEditSupport().postEdit(edit);
        return true;
    }

    private void removeTabs(ModelElementPanel modelElementPanel, boolean clearSpace) {
        if(modelElementPanel.getModelElementType() == ModelElementType.TAB && !clearSpace) {
            ArrayList<ModelElementPanel> panels = new ArrayList<>(modelElementPanel.getTab().getModelElementPanels());
            for(ModelElementPanel panel : panels) {
                if(!panel.getName().equals(editorPanel.getMtsEditorGUI().getCurrentTab().getName())) {
                    panel.setInTab(editorPanel.getMtsEditorGUI().getCurrentTab().getName());
                    editorPanel.getMtsEditorGUI().getCurrentTab().getModelElementPanels().add(panel);
                } else {
                    modelElementPanels.remove(panel);
                }
            }
            ArrayList<ModelElementPanel> removeElements = new ArrayList<>();
            HashMap<ModelElementPanel, ModelElementPanel> newPanels = new HashMap<>();
            for(ModelElementPanel panel : modelElementPanels) {
                if(panel.getModelElementType() == ModelElementType.TAB) {
                    for(ModelElementPanel panel1 : panel.getTab().getModelElementPanels()) {
                        if(panel1.getModelElementType() == ModelElementType.TAB && panel1.getName().equals(modelElementPanel.getName())) {
                            removeElements.add(panel1);
                            if(!panel.getName().equals(editorPanel.getMtsEditorGUI().getCurrentTab().getName())) {
                                ModelElementPanel newPanel = new ModelElementPanel(editorPanel.getMtsEditorGUI().getCurrentTab().getName(), panel1.getLocation(), (JPanel) editorPanel.getMtsEditorGUI().getEditorLayeredPane().getParent(), editorPanel.getMtsEditorGUI().getCurrentTab(), editorPanel.getMtsEditorGUI());
                                newPanels.put(panel, newPanel);
                            }
                        }
                    }
                }
            }
            for(ModelElementPanel panel : removeElements) {
                modelElementPanels.remove(panel);
                editorPanel.removeElement(panel);
            }
            for(Map.Entry<ModelElementPanel, ModelElementPanel> entry : newPanels.entrySet()) {
                entry.getValue().setInTab(entry.getKey().getName());
                addModelElementPanel(entry.getValue());
                entry.getKey().getTab().getModelElementPanels().add(entry.getValue());
                entry.getValue().getTab().updateModelElementPanelsList(modelElementPanels);
            }
            editorPanel.getMtsEditorGUI().removeTab(modelElementPanel);
        }
    }

    public ModelElementPanel getHighlightedPanel() {
        for (ModelElementPanel modelElementPanel : modelElementPanels) {
            if (modelElementPanel.isHighlighted()) {
                return modelElementPanel;
            }
        }
        return null;
    }

    public ModelElementPanel getModelElementPanel(String name) {
        for (ModelElementPanel modelElementPanel : modelElementPanels) {
            if (modelElementPanel.getModelElementType() == ModelElementType.INSTANCE) {
                if (modelElementPanel.getInstance().getName().equals(name)) {
                    return modelElementPanel;
                }
            } else if (modelElementPanel.getModelElementType() == ModelElementType.EXTENSION) {
                if (modelElementPanel.getModelElement().name.equals(name)) {
                    return modelElementPanel;
                }
            } else if(modelElementPanel.getModelElementType() == ModelElementType.ENUM) {
                if(modelElementPanel.getModelElement().name.equals(name)) {
                    return modelElementPanel;
                }
            } else if(modelElementPanel.getModelElementType() == ModelElementType.TAB) {
                if(modelElementPanel.getName().equals(name)) {
                    return modelElementPanel;
                }
            }
        }
        return null;
    }

    /**
     * changes the name of a panel
     *
     * @param panel the ModelElementPanel
     * @param name the new name of the panel
     */
    public void changePanelName(ModelElementPanel panel, String name) {
        String clazz = panel.getName();
        for(ModelElement me : elementHandler.getXmlData().elements.values()) {
            if(me.name.equals(panel.getName())) {
                me.name = name;
            }
        }
        for(ModelElementPanel modelElementPanel : modelElementPanels) {
            if(modelElementPanel.getClazz().equals(clazz)) {
                modelElementPanel.setClazz(name);
            }
            if(modelElementPanel.equals(panel)) {
                modelElementPanel.setName(name);
            }
            if(modelElementPanel.getModelElementType() == ModelElementType.EXTENSION
                    && ((InheretedClass) modelElementPanel.getModelElement()).getSuperClass().equals(clazz)) {
                ((InheretedClass) modelElementPanel.getModelElement()).setSuperClass(name);
            }

        }
    }
}