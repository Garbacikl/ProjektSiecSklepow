import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RadioButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
    private JRadioButton radioButton;
    private JTable table;

    public RadioButtonEditor(JTable table) {
        this.table = table;
        radioButton = new JRadioButton();
        radioButton.addActionListener(this);
    }

    @Override
    public Object getCellEditorValue() {
        return radioButton;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        radioButton = (JRadioButton) value;
        return radioButton;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int row = table.getEditingRow();
        int ilosc = Integer.parseInt((String) table.getValueAt(row, 2));
        float cena = (float) table.getValueAt(row, 1);
        float wartosc = ilosc * cena;
        table.setValueAt(wartosc, row, 4);
        fireEditingStopped();
    }
}


