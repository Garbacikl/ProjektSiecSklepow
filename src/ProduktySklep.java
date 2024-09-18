import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import oracle.jdbc.OracleTypes;
import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduktySklep extends JFrame {
    private JPanel panel;
    private JTable table1;
    private JLabel cenaKoszykaLabel;
    private JButton sprawdzKoszykButtonButton;
    private JTextField podajNazweListyTextField;
    private JButton cofnijButton;
    private JLabel nazwaSklepuLabel;
    private Connection connectionNow;
    private List<Produkt> produkty;
    private int idUzytkownika;
    private int idSklepu;

    public ProduktySklep(int userId, Connection connection, int id_sklepu) {
        setTitle("Produkty w Sklepie");
        setSize(500, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createUIComponents();
        idUzytkownika=userId;
        idSklepu=id_sklepu;
        connectionNow=connection;
        try {
            produkty = pobierzProduktyWSklepie(connection, id_sklepu);
            updateTable();
            updateNazweSklepu();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas ładowania danych: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
        setVisible(true);

    }
    private void wartoscKoszyka() {
        float wynik=0;
        for (int i = 0; i < table1.getRowCount(); i++) {
            JRadioButton radioButton = (JRadioButton) table1.getValueAt(i, 4);
            if (radioButton.isSelected()) {
                float cenaZa=0;
                float cena = (Float) table1.getValueAt(i, 2);
                int ilosc = Integer.parseInt((String) table1.getValueAt(i, 3));
                cenaZa=cena*ilosc;
                table1.setValueAt(cenaZa,i, 5);
            }
        }
        for (int i = 0; i < table1.getRowCount(); i++) {
            JRadioButton radioButton = (JRadioButton) table1.getValueAt(i, 4);
            if (radioButton.isSelected()) {
            wynik=wynik+Integer.parseInt((String) table1.getValueAt(i, 3))*(Float) table1.getValueAt(i, 2);
            }
        }
        cenaKoszykaLabel.setText(String.valueOf(wynik));
    }
    private void dodajListe(Connection connection, int idUser) throws SQLException {
        List<Integer> zaznaczoneProduktyId = new ArrayList<>();
        List<Integer> zaznaczoneProduktyIlosc = new ArrayList<>();
        for (int i = 0; i < table1.getRowCount(); i++) {
            JRadioButton radioButton = (JRadioButton) table1.getValueAt(i, 4);
            if (radioButton.isSelected()) {
                int id_produktu = (Integer) table1.getValueAt(i, 0);
                zaznaczoneProduktyId.add(id_produktu);
                int ilosc = Integer.parseInt((String) table1.getValueAt(i, 3));
                zaznaczoneProduktyIlosc.add(ilosc);
            }
        }


            String nazwaListy = podajNazweListyTextField.getText();
            try {
                int iloscList = pobierzIloscList(connection, idUser);

                if(iloscList<5) {
                    // Tworzenie typów tablicowych
                    ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor("ODCINUMBERLIST", connection);
                    ARRAY produktyArray = new ARRAY(descriptor, connection, zaznaczoneProduktyId.toArray());
                    ARRAY ilosciArray = new ARRAY(descriptor, connection, zaznaczoneProduktyIlosc.toArray());

                    CallableStatement stmt = connection.prepareCall("{ call INSERTNEWLISTAZAKUPOW_Z_PRODUKTAMI(?, ?, ?, ?, ?, ?) }");
                    stmt.setInt(1, idUser);
                    stmt.setString(2, nazwaListy);
                    stmt.setArray(3, produktyArray);
                    stmt.setArray(4, ilosciArray);
                    stmt.setDate(5, new java.sql.Date(System.currentTimeMillis())); // użycie bieżącej daty
                    stmt.setString(6, "Niezrealizowane");

                    stmt.execute();
                    stmt.close();
                    JOptionPane.showMessageDialog(this, "Lista zakupów została zapisana!");
                }
                else {
                    JOptionPane.showMessageDialog(this, "Uzytkownik posiada za duzo list!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Błąd podczas zapisywania listy zakupów: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            }
        }

    public String pobierzNazweSklepu(Connection connection, int idSklepu) throws SQLException {
        String nazwa = null;
        String functionCall = "{ ? = call POBIERZNAZWESKLEPU(?) }"; // Wywołanie funkcji, która zwraca wartość
        try (CallableStatement stmt = connection.prepareCall(functionCall)) {
            stmt.registerOutParameter(1, Types.VARCHAR); // Rejestracja parametru zwracanego przez funkcję
            stmt.setInt(2, idSklepu); // Ustawienie parametru wejściowego

            stmt.execute(); // Wykonanie wywołania funkcji
            nazwa= stmt.getString(1); // Pobranie wartości zwróconej przez funkcję
        }
        return nazwa;
    }

    public void updateNazweSklepu() throws SQLException {
        String nazweSklepu=pobierzNazweSklepu(connectionNow, idSklepu);
        nazwaSklepuLabel.setText(nazweSklepu);
    }
    public List<Produkt> pobierzProduktyWSklepie(Connection connection, int idSklepu) throws SQLException {
        List<Produkt> produkty = new ArrayList<>();
        String procedureCall = "{ call POBIERZPRODUKTYWSKLEPIE2(?, ?) }"; // Użyj poprawnej procedury
        try (CallableStatement stmt = connection.prepareCall(procedureCall)) {
            stmt.setInt(1, idSklepu);
            stmt.registerOutParameter(2, OracleTypes.CURSOR);
            stmt.execute();
            try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
                while (rs.next()) {
                    int id = rs.getInt("id_produktu");
                    String nazwa = rs.getString("nazwa");
                    float cena = rs.getFloat("cena");
                    produkty.add(new Produkt(id,nazwa, cena));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return produkty;
    }


    private void updateTable() {
        String[] columnNames = {"ID produktu", "Nazwa produktu", "Cena", "Ilosc", "Dodaj do koszyka", "Wartosc"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4; // Tylko kolumna "Ilosc" jest edytowalna
            }
        };
        for (Produkt produkt : produkty) {
            JRadioButton radioButton = new JRadioButton("Dodaj");
            radioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wartoscKoszyka();
                }
            });
            Object[] data = {produkt.getId(), produkt.getNazwa(), produkt.getCena(), "0", radioButton, "0"};
            tableModel.addRow(data);
        }
        table1.setModel(tableModel);

        // Ustawienie domyślnego edytora dla kolumny "Ilosc" jako JTextField
        table1.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JTextField()));

        // Ustawienie niestandardowego renderera i edytora dla kolumny "Dodaj do koszyka"
        table1.getColumnModel().getColumn(4).setCellRenderer(new RadioButtonRenderer());
        table1.getColumnModel().getColumn(4).setCellEditor(new RadioButtonEditor(table1));

        table1.revalidate();
    }


    private int pobierzIloscList(Connection connection, int idUser) throws SQLException {
        int iloscList = 0;
        String sql = "SELECT LICZBALISTZAKUPOW(?) AS iloscList";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idUser);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    iloscList = rs.getInt("iloscList");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Optionally rethrow or handle exception as needed
        }

        return iloscList;
    }

    private void createUIComponents() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        table1 = new JTable();
        cenaKoszykaLabel = new JLabel("Wartosc koszyka: 0");
        podajNazweListyTextField = new JTextField("Nazwa listy");
        sprawdzKoszykButtonButton = new JButton("Dodaj liste");
        cofnijButton = new JButton("Wstecz");

        // Dodanie JScrollPane dla przewijania tabeli
        JScrollPane scrollPane = new JScrollPane(table1);
        panel.add(scrollPane);
        panel.add(nazwaSklepuLabel);
        panel.add(podajNazweListyTextField);
        panel.add(cenaKoszykaLabel);
        panel.add(sprawdzKoszykButtonButton);
        panel.add(cofnijButton);
        add(panel);

        // Dodanie listenera do przycisku sprawdzKoszykButtonButton
        sprawdzKoszykButtonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dodajListe(connectionNow, idUzytkownika);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        cofnijButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Sklepy sklepyInstance = null;
                if (sklepyInstance == null) {
                    sklepyInstance = new Sklepy(idUzytkownika, connectionNow); // Zakładając, że Sklepy ma konstruktor bezargumentowy
                }
                dispose();
                sklepyInstance.setVisible(true); // Upewnij się, że okno Sklepy jest widoczne
            }
        });
    }



}
