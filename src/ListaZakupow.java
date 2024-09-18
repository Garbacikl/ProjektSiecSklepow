import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListaZakupow extends JFrame{
    private JButton cofnijButton;
    private JButton usunButton;
    private JPanel panel;
    private List<Lista> lista;
    private JTable table1;
    private JLabel nazwaKoszyka;
    private JLabel statusKoszyka;
    private JLabel wartoscKoszykaLabel;
    private Connection connectionNow;
    private int idUzytkownika;
    private int idListy;

    public ListaZakupow(int userId, Connection connection, int id_listy) {
        setTitle("Produkty w Liscie");
        setSize(500, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createUIComponents();
        idUzytkownika=userId;
        idListy=id_listy;
        connectionNow=connection;

        try {
            lista = pobierzProduktyWLiscie(connection, idListy);
            updateTable();
            updateStatus();
            updateNazwaListy();
            updateWrtoscKoszyka();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas ładowania danych: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }

        setVisible(true);

    }





    public List<Lista> pobierzProduktyWLiscie(Connection connection, int idListy) throws SQLException {
        List<Lista> lista = new ArrayList<>();
        String procedureCall = "{ call POBIERZPRODUKTYWLISCIE(?, ?) }"; // Użyj poprawnej procedury
        try (CallableStatement stmt = connection.prepareCall(procedureCall)) {
            stmt.setInt(1, idListy);
            stmt.registerOutParameter(2, OracleTypes.CURSOR);
            stmt.execute();
            try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
                while (rs.next()) {
                    int id = rs.getInt("id_produktu");
                    String nazwa = rs.getString("nazwa");
                    float cena = rs.getFloat("cena");
                    int ilosc = rs.getInt("ilosc_produktu");
                    lista.add(new Lista(id,nazwa, cena, ilosc));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return lista;
    }
    private void updateTable() {
        String[] columnNames = {"ID produktu", "Nazwa produktu", "Cena", "Ilosc"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        for (Lista lista : lista) {
            Object[] data = {lista.getId(), lista.getNazwa(), lista.getCena(), lista.getIlosc()};
            tableModel.addRow(data);
        }
        table1.setModel(tableModel);

        table1.revalidate();
    }
    public String pobierzStatus(Connection connection, int idListy) throws SQLException {
        String status = null;
        String functionCall = "{ ? = call POBIERZSTATUS(?) }"; // Wywołanie funkcji, która zwraca wartość
        try (CallableStatement stmt = connection.prepareCall(functionCall)) {
            stmt.registerOutParameter(1, java.sql.Types.VARCHAR); // Rejestracja parametru zwracanego przez funkcję
            stmt.setInt(2, idListy); // Ustawienie parametru wejściowego

            stmt.execute(); // Wykonanie wywołania funkcji
            status = stmt.getString(1); // Pobranie wartości zwróconej przez funkcję
        }
        return status;
    }

    public void updateStatus() throws SQLException {
        String status=pobierzStatus(connectionNow, idListy);
        statusKoszyka.setText("Status:"+status);
    }

    public String pobierzNazwaListy(Connection connection, int idListy) throws SQLException {
        String status = null;
        String functionCall = "{ ? = call POBIERZNAZWELISTY(?) }"; // Wywołanie funkcji, która zwraca wartość
        try (CallableStatement stmt = connection.prepareCall(functionCall)) {
            stmt.registerOutParameter(1, java.sql.Types.VARCHAR); // Rejestracja parametru zwracanego przez funkcję
            stmt.setInt(2, idListy); // Ustawienie parametru wejściowego

            stmt.execute(); // Wykonanie wywołania funkcji
            status = stmt.getString(1); // Pobranie wartości zwróconej przez funkcję
        }
        return status;
    }

    public void updateNazwaListy() throws SQLException {
        String nazwaListy=pobierzNazwaListy(connectionNow, idListy);
        nazwaKoszyka.setText("Nazwa:"+nazwaListy);
    }

    public Float pobierzWartoscKoszyka(Connection connection, int idListy) throws SQLException {
        float wartoscKoszyka = 0;
        String functionCall = "{ ? = call CENALISTY(?) }"; // Wywołanie funkcji, która zwraca wartość
        try (CallableStatement stmt = connection.prepareCall(functionCall)) {
            stmt.registerOutParameter(1, Types.FLOAT); // Rejestracja parametru zwracanego przez funkcję
            stmt.setInt(2, idListy); // Ustawienie parametru wejściowego

            stmt.execute(); // Wykonanie wywołania funkcji
            wartoscKoszyka= stmt.getFloat(1); // Pobranie wartości zwróconej przez funkcję
        }
        return wartoscKoszyka;
    }

    public void updateWrtoscKoszyka() throws SQLException {
        float wartoscKoszyka=pobierzWartoscKoszyka(connectionNow, idListy);
        String wartoscKoszykaString=String.valueOf(wartoscKoszyka);
        wartoscKoszykaLabel.setText("Wartosc:"+wartoscKoszykaString);
    }

    private void deleteLista(int idListy){
        String procedureCall = "{ call DELETELISTA(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, idListy);
            callableStatement.execute();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createUIComponents() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        table1 = new JTable();
        cofnijButton = new JButton("Wstecz");
        usunButton = new JButton("Usun Liste");

        // Dodanie JScrollPane dla przewijania tabeli
        JScrollPane scrollPane = new JScrollPane(table1);
        panel.add(scrollPane);
        panel.add(statusKoszyka);
        panel.add(nazwaKoszyka);
        panel.add(wartoscKoszykaLabel);
//        panel.add(sprawdzKoszykButtonButton);
        panel.add(cofnijButton);
        panel.add(usunButton);
        add(panel);


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
        usunButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int listaID = idListy;
                deleteLista(listaID);
                Sklepy sklepyInstance = null;
                if (sklepyInstance == null) {
                    sklepyInstance = new Sklepy(idUzytkownika, connectionNow); // Zakładając, że Sklepy ma konstruktor bezargumentowy
                }
                dispose();
                sklepyInstance.setVisible(true);
            }
        });
    }
}
