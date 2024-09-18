import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class RadioButtonRenderer implements TableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof JRadioButton) {
            JRadioButton radioButton = (JRadioButton) value;
            if (isSelected) {
                radioButton.setBackground(table.getSelectionBackground());
                radioButton.setForeground(table.getSelectionForeground());
            } else {
                radioButton.setBackground(table.getBackground());
                radioButton.setForeground(table.getForeground());
            }
            return radioButton;
        }
        return null;
    }
}


