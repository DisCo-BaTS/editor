package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {

    private final int integer;

    public CustomTableCellRenderer(int integer) {
        this.integer = integer;

    }

    public Component getTableCellRendererComponent(JTable table, Object
            value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (row < integer) {
            cell.setForeground(new Color(117, 117, 117));
        }
        else {
            cell.setForeground(new Color(187, 187, 187));
        }
        return cell;
    }
}

