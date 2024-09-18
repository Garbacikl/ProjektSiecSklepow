import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TextFieldRenderer extends JTextField implements TableCellRenderer {
    public TextFieldRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value != null) {
            setText(value.toString());
        } else {
            setText("0"); // Ustawienie domyślnej wartości "0"
        }
        return this;
    }
}

