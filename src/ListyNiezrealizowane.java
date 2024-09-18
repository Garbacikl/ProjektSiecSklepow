import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

import oracle.jdbc.OracleTypes;

public class ListyNiezrealizowane extends JFrame {
    private int id;
    private Connection connectionNow;
    private JButton cofnijButton = new JButton("Wstecz");
    private JTable listaNiezrealizowanaTable;
    private JScrollPane scrollPane;
    private JComboBox <String> nazwyList;
    private JButton zmienJButton;

    public ListyNiezrealizowane(int userId, Connection connection) {
        this.id = userId;
        this.connectionNow = connection;

        initializeLayout();
        createUIComponents();
        setupListeners();
        loadData();
    }

    private void initializeLayout() {
        setTitle("Listy Niezrealizowane");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void createUIComponents() {
        JLabel headerLabel = new JLabel("Listy Niezrealizowane", JLabel.CENTER);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 20));
        add(headerLabel, BorderLayout.NORTH);

        listaNiezrealizowanaTable = new JTable();
        scrollPane = new JScrollPane(listaNiezrealizowanaTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        // ComboBox
        nazwyList= new JComboBox<>();
        buttonPanel.add(nazwyList);

        // New Button
        zmienJButton = new JButton("Zmien status");
        buttonPanel.add(zmienJButton);
        buttonPanel.add(cofnijButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        cofnijButton.addActionListener(ActionEvent -> {
            AdminPanel adminPanel = new AdminPanel(id, connectionNow);
            dispose();
            adminPanel.setVisible(true);
        });

        zmienJButton.addActionListener(ActionEvent ->{
            int id=findListaByName();
            changeStatus(id);
        });
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
    private void changeStatus(int id){
        String procedureCall = "{ call UPDATELISTAZAKUPOW(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, id);
            callableStatement.execute();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        String procedureCall = "{ call ReadNiezrealizowaneListy(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();

            try (ResultSet rs = (ResultSet) callableStatement.getObject(1)) {
                // Przygotowanie modelu tabeli do wyświetlenia danych
                DefaultTableModel model = new DefaultTableModel(new String[]{"ID Listy", "ID Użytkownika", "Data Utworzenia", "Nazwa", "Status"}, 0);
                while (rs.next()) {
                    String listDetails = rs.getString("NAZWA");
                    nazwyList.addItem(listDetails);
                    model.addRow(new Object[]{
                        rs.getInt("ID_LISTY"),
                        rs.getInt("ID_UZYTKOWNIKA"),
                        rs.getDate("DATA_UTWORZENIA"),
                        rs.getString("NAZWA"),
                        rs.getString("STATUS")
                    });
                }
                listaNiezrealizowanaTable.setModel(model);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
