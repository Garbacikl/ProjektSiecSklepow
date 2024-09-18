import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ListyUzytkownika extends JFrame {
    private int adminID;
    private Connection connectionNow;
    private JTable listaUzytkownikaTable;
    private JScrollPane scrollPane;
    private int userID;
    private JComboBox<String> nazwyList;
    private JButton pokaz;

    private JButton cofnijButton=new JButton("Wstecz");
    public ListyUzytkownika(int admin, Connection connection, int user){
        this.adminID=admin;
        this.connectionNow=connection;
        this.userID=user;


        initializeLayout();
        createUIComponents();
        setupListeners();
        loadData(userID);
        readNazwyListy(userID);
    }

    private void initializeLayout() {
        setTitle("Listy Uzykotwnika - ID użytkownika: " +userID);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void createUIComponents() {
        // Header label
        JLabel headerLabel = new JLabel("Listy Uzytkownika " + userID, JLabel.CENTER);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 20));
        add(headerLabel, BorderLayout.NORTH);

        // Table and scroll pane
        listaUzytkownikaTable = new JTable();
        scrollPane = new JScrollPane(listaUzytkownikaTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();

        // ComboBox
        nazwyList= new JComboBox<>();
        buttonPanel.add(nazwyList);

        // New Button
        pokaz = new JButton("Pokaz");
        buttonPanel.add(pokaz);

        // Existing back button
        buttonPanel.add(cofnijButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
    public int findListaByName() {
        String nazwaListy=getNazwaListy();
        String procedureCall = "{ ? = call FINDLISTAPBYNAME(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, nazwaListy);
            callableStatement.execute();
            int ListaId = callableStatement.getInt(1);
            return ListaId;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
    public String getNazwaListy() {
        return (String) nazwyList.getSelectedItem();
    }


    private void readNazwyListy(int idUser) {
        String procedureCall = "{ call ReadNazwyList(?,?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.setInt(2, idUser);
            callableStatement.execute();
            ResultSet rs = (ResultSet) callableStatement.getObject(1);
            while (rs.next()) {
                String listDetails = rs.getString("NAZWA");
                nazwyList.addItem(listDetails);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void setupListeners() {
        cofnijButton.addActionListener(ActionEvent -> {
            AdminPanel adminPanel = new AdminPanel(adminID, connectionNow);
            dispose();
            adminPanel.setVisible(true);
        });
        pokaz.addActionListener(ActionEvent -> {
            int id=findListaByName();
            loadProdukty(id);
        });
    }

    private void loadProdukty(int idListy) {
        String procedureCall = "{ call READLISTAPRODUKTOW(?, ?) }";

        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, idListy);
            callableStatement.registerOutParameter(2, OracleTypes.CURSOR);
            callableStatement.execute();

            try (ResultSet rs = (ResultSet) callableStatement.getObject(2)) {
                // Przygotowanie modelu tabeli do wyświetlenia danych
                DefaultTableModel model = new DefaultTableModel(new String[]{"ID Produktu", "Nazwa", "Ilość"}, 0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("ID_PRODUKTU"),
                        rs.getString("NAZWA"),
                        rs.getInt("ILOSC_PRODUKTU")
                    });
                }

                JTable table = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(table);
                JOptionPane.showMessageDialog(this, scrollPane, "Produkty", JOptionPane.INFORMATION_MESSAGE);

            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void loadData(int userId) {
        String procedureCall = "{ call ReadListyUzytkownika(?, ?) }";

        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(2, userId); // Set the userId input parameter
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR); // Register the output cursor parameter
            callableStatement.execute();

            try (ResultSet rs = (ResultSet) callableStatement.getObject(1)) { // Get the cursor as ResultSet from the second parameter
                // Prepare table model to display data
                DefaultTableModel model = new DefaultTableModel(new String[]{"ID Listy", "Data Utworzenia", "Nazwa", "Status", "Liczba Produktow"}, 0);

                while (rs.next()) {
                    // Add row to the table model
                    model.addRow(new Object[]{
                        rs.getInt("ID_LISTY"),
                        rs.getDate("DATA_UTWORZENIA"),
                        rs.getString("NAZWA"),
                        rs.getString("STATUS"),
                        rs.getInt("LICZBA_PRODUKTOW")
                    });
                }
                listaUzytkownikaTable.setModel(model);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}
