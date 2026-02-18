package handlers;

import gui.ListItem;
import gui.ListTransferHandler;
import gui.MTSEditorGUI;
import gui.ModelElementType;
import gui.modelelements.AssociationElementPanel;
import gui.modelelements.ModelElementPanel;
import gui.popups.CreateNamedElementDialog;
import gui.popups.ElementExistsDialog;
import model.InheretedClass;
import model.InstanciatedClass;
import model.ModelEnum;
import saving.SaveState;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class TemplateHandler {
    private static final String TEMPLATE_FOLDER = System.getProperty("java.io.tmpdir") + File.separator + "tssmeta" + File.separator + "templates"; //location of templates
    private static boolean repeatForAllElements;
    private static String repeatAction;
    private static boolean abort = false;

    /**
     * Creates a template in the templates folder
     *
     * @param mtsEditorGUI
     * @param templateName name of the template
     */
    public static void saveTemplate(MTSEditorGUI mtsEditorGUI, String templateName) {
        createTemplatesFolder();
        File fileToSave = new File(TEMPLATE_FOLDER, templateName + ".tss");
        SaveState saveState = new SaveState();

        String[][] associationPanels = new String[mtsEditorGUI.getEditorPanel().getAssociationElementPanels().size()][2];
        for (int i = 0; i < associationPanels.length; i++) {
            if(mtsEditorGUI.getEditorPanel().getAssociationElementPanels().get(i).getStartModelElementPanel().isHighlighted()
                    && mtsEditorGUI.getEditorPanel().getAssociationElementPanels().get(i).getEndModelElementPanel().isHighlighted()) {
                associationPanels[i][0] = mtsEditorGUI.getEditorPanel().getAssociationElementPanels().get(i).getStartModelElementPanel().getNameText();
                associationPanels[i][1] = mtsEditorGUI.getEditorPanel().getAssociationElementPanels().get(i).getEndModelElementPanel().getNameText();
            }
        }
        saveState.associationElementPanels = associationPanels;

        HashMap<InheretedClass, Point> inheretedClassPointHashMap = new HashMap<>();
        HashMap<InstanciatedClass, Point> instanciatedClassPointHashMap = new HashMap<>();
        HashMap<ModelEnum, Point> addedEnumsPointHashMap = new HashMap<>();

        for (ModelElementPanel elementPanel : mtsEditorGUI.getElementPanelHandler().getModelElementPanels()) {
            if(elementPanel.isHighlighted()) {
                if (elementPanel.getModelElementType() == ModelElementType.EXTENSION) {
                    inheretedClassPointHashMap.put((InheretedClass) elementPanel.getModelElement(), elementPanel.getLocation());
                } else if (elementPanel.getModelElementType() == ModelElementType.INSTANCE) {
                    instanciatedClassPointHashMap.put(elementPanel.getInstance(), elementPanel.getLocation());
                } else if (elementPanel.getModelElementType() == ModelElementType.ENUM) {
                    addedEnumsPointHashMap.put((ModelEnum) elementPanel.getModelElement(), elementPanel.getLocation());
                }
            }
        }

        saveState.inheretedClassesPositions = inheretedClassPointHashMap;
        saveState.instanciatedClassesPositions = instanciatedClassPointHashMap;
        saveState.addedEnumPositions = addedEnumsPointHashMap;

        SaveState.save(saveState, fileToSave.toString());
        Logger.getInstance().writeInfo("Template \"" + templateName + "\" successfully saved");
    }

    /**
     * loads a template from the template folder
     *
     * @param mtsEditorGUI
     * @param templateName name of the template
     */
    public static void loadTemplate(MTSEditorGUI mtsEditorGUI, String templateName) {
        createTemplatesFolder();
        repeatForAllElements = false;
        abort = false;
        File fileToLoad = new File(TEMPLATE_FOLDER, templateName + ".tss");
        SaveState saveState = SaveState.load(fileToLoad.getAbsolutePath());
        if (saveState == null) {
            return;
        }
        ElementHandler elementHandler = ElementHandler.getInstance();

        CopyOnWriteArrayList<InheretedClass> loadedInheretedClasses = new CopyOnWriteArrayList<>(saveState.inheretedClassesPositions.keySet());
        CopyOnWriteArrayList<InheretedClass> currentInheretedClasses = new CopyOnWriteArrayList<>(elementHandler.getInheretedClasses());
        CopyOnWriteArrayList<InheretedClass> newInheretedClasses = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<InstanciatedClass> loadedInstanciatedClasses = new CopyOnWriteArrayList<>(saveState.instanciatedClassesPositions.keySet());
        CopyOnWriteArrayList<InstanciatedClass> currentInstanciatedClasses = new CopyOnWriteArrayList<>(elementHandler.getInstanciatedClasses());
        CopyOnWriteArrayList<InstanciatedClass> newInstanciatedClasses = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<ModelEnum> modelEnums = new CopyOnWriteArrayList<>(saveState.addedEnumPositions.keySet());
        CopyOnWriteArrayList<ModelEnum> currentModelEnums = new CopyOnWriteArrayList<>(elementHandler.getModelEnums());
        CopyOnWriteArrayList<ModelEnum> newModelEnums = new CopyOnWriteArrayList<>();
        HashMap<String, String> changedValues = new HashMap<>();
        ArrayList<String> usedNames = new ArrayList<>();
        for(InheretedClass inheretedClass : loadedInheretedClasses) {
            usedNames.add(inheretedClass.name);
        }
        for(InstanciatedClass instanciatedClass : loadedInstanciatedClasses) {
            usedNames.add(instanciatedClass.getName());
        }
        for(ModelEnum modelEnum : modelEnums) {
            usedNames.add(modelEnum.name);
        }

        for(ModelEnum modelEnum : modelEnums) {
            boolean equals = false;
            for(ModelEnum modelEnum1 : currentModelEnums) {
                if(modelEnum.name.equals(modelEnum1.name)) {
                    usedNames.addAll(changedValues.values());
                    ElementExistsDialog dialog = showElementExistsDialog(mtsEditorGUI, modelEnum.name, usedNames);
                    if(dialog.isResult()) {
                        if(dialog.isChangeNameButton()) {
                            changedValues.put(modelEnum.name, dialog.getNewName());
                            modelEnum.name = dialog.getNewName();
                        }
                        if(!dialog.isDeleteButtonSelected()) {
                            newModelEnums.add(modelEnum);
                        } else {
                            currentModelEnums.remove(modelEnum);
                        }
                        equals = true;
                        break;
                    }
                }
            }
            if(!equals) {
                newModelEnums.add(modelEnum);
            }
        }
        for(InheretedClass inheretedClass : loadedInheretedClasses) {
            boolean equals = false;
            for(InheretedClass inheretedClass1 : currentInheretedClasses) {
                if (inheretedClass.name.equals(inheretedClass1.name)) {
                    usedNames.addAll(changedValues.values());
                    ElementExistsDialog dialog = showElementExistsDialog(mtsEditorGUI, inheretedClass.name, usedNames);
                    if(dialog.isResult()) {
                        if (dialog.isChangeNameButton()) {
                            changedValues.put(inheretedClass.name, dialog.getNewName());
                            inheretedClass.name = dialog.getNewName();
                        }
                        if (!dialog.isDeleteButtonSelected()) {
                            newInheretedClasses.add(inheretedClass);
                        } else {
                            currentInheretedClasses.remove(inheretedClass);
                        }
                        equals = true;
                        break;
                    }
                }
            }
            if(!equals) {
                newInheretedClasses.add(inheretedClass);
            }
        }
        for(InstanciatedClass instanciatedClass : loadedInstanciatedClasses) {
            boolean equals = false;
            for(InstanciatedClass instanciatedClass1 : currentInstanciatedClasses) {
                if(instanciatedClass.getName().equals(instanciatedClass1.getName())) {
                    usedNames.addAll(changedValues.values());
                    ElementExistsDialog dialog = showElementExistsDialog(mtsEditorGUI, instanciatedClass.getName(), usedNames);
                    if(dialog.isResult()) {
                        if (dialog.isChangeNameButton()) {
                            changedValues.put(instanciatedClass.getName(), dialog.getNewName());
                            instanciatedClass.setName(dialog.getNewName());
                        }
                        if (!dialog.isDeleteButtonSelected()) {
                            newInstanciatedClasses.add(instanciatedClass);
                        } else {
                            currentInstanciatedClasses.remove(instanciatedClass);
                        }
                        equals = true;
                        break;
                    }
                }
            }
            if(!equals) {
                newInstanciatedClasses.add(instanciatedClass);
            }
        }

        for(InheretedClass inheretedClass : newInheretedClasses) {
            for(Map.Entry<String, String> entry : changedValues.entrySet()) {
                if(inheretedClass.getSuperClass().equals(entry.getKey())) {
                    inheretedClass.setSuperClass(entry.getValue());
                }
            }
        }
        for(InstanciatedClass instanciatedClass : newInstanciatedClasses) {
            for(Map.Entry<String, String> entry : changedValues.entrySet()) {
                if(instanciatedClass.getInstanceOf().name.equals(entry.getKey())) {
                    instanciatedClass.getInstanceOf().name = entry.getValue();
                }
            }
        }
        for(String[] associationElementPanel : saveState.associationElementPanels) {
            for(Map.Entry<String, String> entry : changedValues.entrySet()) {
                if(associationElementPanel[0] != null && associationElementPanel[0].equals(entry.getKey())) {
                    associationElementPanel[0] = entry.getValue();
                }
                if(associationElementPanel[1] != null && associationElementPanel[1].equals(entry.getKey())) {
                    associationElementPanel[1] = entry.getValue();
                }
            }
        }

        if(abort) {
            abort = false;
        } else {
            for(ModelEnum modelEnum : newModelEnums) {
                CreateNamedElementDialog createNamedElementDialog = new CreateNamedElementDialog(modelEnum.name, null, true, modelEnum.hasGenericType);
                createNamedElementDialog.pack();
                createNamedElementDialog.setVisible(true);
                if(createNamedElementDialog.getResult() != null) {
                    Point point = null;
                    for(Map.Entry<ModelEnum, Point> entry : saveState.addedEnumPositions.entrySet()) {
                        if(entry.getKey().name.equals(modelEnum.name)) {
                            point = entry.getValue();
                        }
                    }
                    modelEnum.id = "ENUMDUMMY";
                    ElementHandler.getInstance().addEnum(modelEnum);
                    ListTransferHandler.createModelElementPanel(new ListItem(modelEnum.name, modelEnum.id), point, null, createNamedElementDialog, null);
                }
                newModelEnums.remove(modelEnum);
            }

            boolean createNextClass = true;
            while (createNextClass) {
                int oldSize = newInheretedClasses.size();
                for(InheretedClass inheretedClass : newInheretedClasses) {
                    for(String modelElement : ElementHandler.getInstance().getAllModelElementNames()) {
                        if(inheretedClass.getSuperClass().equals(modelElement)) {
                            CreateNamedElementDialog createNamedElementDialog = new CreateNamedElementDialog(inheretedClass.name, inheretedClass.getSuperClass(), false, inheretedClass.hasGenericType);
                            createNamedElementDialog.pack();
                            createNamedElementDialog.setVisible(true);
                            if(createNamedElementDialog.getResult() != null) {
                                Point point = null;
                                for(Map.Entry<InheretedClass, Point> entry : saveState.inheretedClassesPositions.entrySet()) {
                                    if(entry.getKey().name.equals(inheretedClass.name)) {
                                        point = entry.getValue();
                                    }
                                }
                                ListTransferHandler.createModelElementPanel(new ListItem(inheretedClass.name, inheretedClass.id), point, null, createNamedElementDialog, inheretedClass.getSuperClass());
                            }
                            newInheretedClasses.remove(inheretedClass);
                        }
                    }
                }
                if(newInheretedClasses.isEmpty() || newInheretedClasses.size() == oldSize) {
                    createNextClass = false;
                }
            }

            for(InstanciatedClass instanciatedClass : newInstanciatedClasses) {
                CreateNamedElementDialog createNamedElementDialog = new CreateNamedElementDialog(instanciatedClass.getName(), instanciatedClass.getInstanceOf().name, true, instanciatedClass.getInstanceOf().hasGenericType);
                createNamedElementDialog.pack();
                createNamedElementDialog.setVisible(true);
                if(createNamedElementDialog.getResult() != null) {
                    Point point = null;
                    for(Map.Entry<InstanciatedClass, Point> entry : saveState.instanciatedClassesPositions.entrySet()) {
                        if(entry.getKey().getName().equals(instanciatedClass.getName())) {
                            point = entry.getValue();
                        }
                    }
                    ModelElementPanel modelElementPanel = new ModelElementPanel(instanciatedClass.getName(), instanciatedClass.getInstanceOf().name, mtsEditorGUI, (JPanel) mtsEditorGUI.getEditorLayeredPane().getParent(), point, ModelElementType.INSTANCE);
                    if (!instanciatedClass.getSubTypes().isEmpty()) {
                        modelElementPanel.setSubTypes(instanciatedClass.getSubTypes());
                    }
                    ElementHandler.getInstance().addInstance(instanciatedClass);
                    modelElementPanel.setInstance(instanciatedClass);
                    modelElementPanel.setColor(instanciatedClass.getInstanceOf().color);
                    mtsEditorGUI.elementSelected(modelElementPanel);
                    mtsEditorGUI.getElementPanelHandler().addModelElementPanel(modelElementPanel);
                }
                newInstanciatedClasses.remove(instanciatedClass);
            }

            for (int i = 0; i < saveState.associationElementPanels.length; i++) {
                ModelElementPanel a = mtsEditorGUI.getElementPanelHandler().getModelElementPanel(saveState.associationElementPanels[i][0]);
                ModelElementPanel b = mtsEditorGUI.getElementPanelHandler().getModelElementPanel(saveState.associationElementPanels[i][1]);
                if(a == null || b == null) {
                    continue;
                }
                boolean elementExists = false;
                for(AssociationElementPanel associationElementPanel : mtsEditorGUI.getEditorPanel().getAssociationElementPanels()) {
                    if(associationElementPanel.getStartModelElementPanel().getName().equals(a.getName()) && associationElementPanel.getEndModelElementPanel().getName().equals(b.getName())) {
                        elementExists = true;
                        break;
                    }
                }
                if(elementExists) {
                    continue;
                }

                AssociationElementPanel associationElementPanel = new AssociationElementPanel(a, b, mtsEditorGUI);
                if ((a.getModelElementType() == ModelElementType.EXTENSION && b.getModelElementType() == ModelElementType.INSTANCE)
                        || (a.getModelElementType() == ModelElementType.INSTANCE && b.getModelElementType() == ModelElementType.EXTENSION)) {
                    associationElementPanel.setDefaultValue();
                    if (a.getModelElementType() == ModelElementType.INSTANCE && a.getModelElement() != b.getModelElement()) {
                        if(!a.getModelElement().name.equals(b.getName())) {
                            a.setDefaultValue();
                        } else {
                            associationElementPanel.setUseInstanceOf(true);
                        }
                    }
                    if (b.getModelElementType() == ModelElementType.INSTANCE && a.getModelElement() != b.getModelElement()) {
                        b.setDefaultValue();
                    }
                }
                a.setAssociationElementGraphic(associationElementPanel);
                b.setAssociationElementGraphic(associationElementPanel);
                mtsEditorGUI.getEditorPanel().addAssociation(associationElementPanel);
            }

            Logger.getInstance().writeInfo("Template \"" + templateName + "\" successfully loaded");
        }
    }

    private static ElementExistsDialog showElementExistsDialog(MTSEditorGUI mtsEditorGUI, String name, ArrayList<String> usedNames) {
        ElementExistsDialog elementExistsDialog = new ElementExistsDialog(name, repeatForAllElements, repeatAction, usedNames);
        if(!abort) {
            elementExistsDialog.pack();
            elementExistsDialog.setLocationRelativeTo(mtsEditorGUI);
            elementExistsDialog.setVisible(true);
            if(elementExistsDialog.isRepeatForAllElements()) {
                repeatForAllElements = elementExistsDialog.isRepeatForAllElements();
                repeatAction = elementExistsDialog.getRepeatedAction();
            }
            if(elementExistsDialog.isAbort()) {
                abort = true;
                Logger.getInstance().writeWarning("Template could not be loaded");
            }
        }

        return elementExistsDialog;
    }

    private static void createTemplatesFolder() {
        Path templateFolderPath = Paths.get(TEMPLATE_FOLDER);
        if (!templateFolderPath.toFile().exists()) {
            templateFolderPath.toFile().mkdirs();
        }
    }

    /**
     * Looks if the name of the template already exists in the folder
     *
     * @param templateName name of the template
     * @return boolean if the name of the template already exists
     */
    public static boolean doesTemplateExist(String templateName) {
        File templateFile = new File(TEMPLATE_FOLDER, templateName + ".tss");
        return templateFile.exists();
    }

    public static String getTemplateFolder() {
        return TEMPLATE_FOLDER;
    }
}
