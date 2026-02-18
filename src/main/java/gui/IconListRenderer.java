package gui;

import handlers.ElementHandler;
import model.ModelEnum;

import javax.swing.*;
import java.awt.*;

public class IconListRenderer extends DefaultListCellRenderer {
    private final ElementHandler elementHandler;

    public IconListRenderer() {
        super();
        this.elementHandler = ElementHandler.getInstance();
    }

    private ImageIcon scale(String p, int cellHeight) {
        cellHeight *= 1.35;
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(p)); // transform it
        Image newimg = image.getScaledInstance(cellHeight, cellHeight, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
        ImageIcon imageIcon = new ImageIcon(newimg);  // transform it back

        return imageIcon;
    }

    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        ImageIcon icon = this.getIcon(list, value, index, isSelected, cellHasFocus);
        label.setIcon(icon);
        return label;
    }

    protected ImageIcon getIcon(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value.getClass() != ListItem.class) {
            return null;
        }
        //Class Icon
        if (elementHandler.getElement(((ListItem) value).getId()) != null && elementHandler.getElement(((ListItem) value).getId()).getClass() != ModelEnum.class) {
            ImageIcon icon = scale("classIcon.png", list.getFont().getSize());
            return icon;
        }
        //Enum Icon
        else if (elementHandler.getElement(((ListItem) value).getId()) != null && elementHandler.getElement(((ListItem) value).getId()).getClass() == ModelEnum.class) {
            ImageIcon icon = scale("enumIcon.png", list.getFont().getSize());
            return icon;
        } else {
            return null;
        }
    }
}
