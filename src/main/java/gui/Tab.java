package gui;

import gui.modelelements.ModelElementPanel;

import java.io.Serializable;
import java.util.ArrayList;

public class Tab implements Serializable {
    private final String name;
    private ArrayList<ModelElementPanel> modelElementPanels;

    public Tab(String name, ArrayList<ModelElementPanel> modelElementPanels) {
        this.name = name;
        if(modelElementPanels == null) {
            modelElementPanels = new ArrayList<>();
        }
        this.modelElementPanels = modelElementPanels;
        for(ModelElementPanel modelElementPanel : this.modelElementPanels) {
            modelElementPanel.setInTab(name);
        }
    }

    public String getName() {
        return name;
    }

    public ArrayList<ModelElementPanel> getModelElementPanels() {
        return modelElementPanels;
    }

    public void updateModelElementPanelsList(ArrayList<ModelElementPanel> modelElementPanels) {
        this.modelElementPanels = new ArrayList<>();
        for(ModelElementPanel modelElementPanel : modelElementPanels) {
            if(modelElementPanel.getInTab().equals(name)) {
                this.modelElementPanels.add(modelElementPanel);
            }
        }
    }
}
