import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SklepyPanel extends JFrame {
    private Connection connectionNow;
    private int id;
    private JButton cofnijButton=new JButton("Wstecz");
    private JButton dodajButton = new JButton("Dodaj");
    private JButton updateButton = new JButton("Update");
    private JButton deleteButton = new JButton("Usun");
    private JTable listaSklepowTable;
    private JComboBox <String> sklep;
    private JScrollPane scrollPane;
    private JTextField nazwaField;
    private JTextField adresField;
    private JComboBox <String> marka;
    public SklepyPanel(int adminID, Connection connection){
        this.connectionNow=connection;
        this.id=adminID;

        initializeLayout();
        createUIComponents();
        readSklepy();

        setupListeners();
        loadData();
    }
    private void initializeLayout() {
        setTitle("Panel Sklepow");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void createUIComponents() {
        JLabel headerLabel = new JLabel("Sklepy", JLabel.CENTER);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 20));
        add(headerLabel, BorderLayout.NORTH);

        listaSklepowTable = new JTable();
        scrollPane = new JScrollPane(listaSklepowTable);
        add(scrollPane, BorderLayout.CENTER);

        marka = new JComboBox<String>();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cofnijButton);
        buttonPanel.add(dodajButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    private void setupListeners() {
        cofnijButton.addActionListener(ActionEvent -> {
            AdminPanel adminPanel = new AdminPanel(id, connectionNow);
            dispose();
            adminPanel.setVisible(true);
        });
        dodajButton.addActionListener(e -> pokazOknoDodawaniaSklepu());


        deleteButton.addActionListener(e -> pokazOknoUsuwanaSklepu(marka));

        updateButton.addActionListener(e -> pokazOknoAktualizowaniaSklepu());
    }
    private void pokazOknoDodawaniaSklepu() {

        nazwaField = new JTextField(10);
        adresField = new JTextField(10);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Nazwa:"));
        myPanel.add(nazwaField);
        myPanel.add(Box.createHorizontalStrut(15)); // odstęp
        myPanel.add(new JLabel("Adres:"));
        myPanel.add(adresField);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Wprowadź dane sklepu", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String nazwa = nazwaField.getText();
            String adres = adresField.getText();
            addNewSklep(nazwa, adres);
        }
    }
    private void addNewSklep(String p_nazwa,String p_adres){
        String procedureCall = "{ call INSERTNEWSKLEP(?,?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setString(1, p_nazwa);
            callableStatement.setString(2, p_adres);
            callableStatement.execute();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void pokazOknoUsuwanaSklepu(JComboBox<String> sklep) {
        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Sklep:"));
        myPanel.add(marka);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Wprowadź dane sklepu", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String produktString = (String) marka.getSelectedItem();
            int markaInt = findSklepByName(produktString);
            deleteSklep(markaInt);

        }
    }

    private void deleteSklep(int idSklepu){
        String procedureCall = "{ call DELETESKLEP(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, idSklepu);
            callableStatement.execute();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pokazOknoAktualizowaniaSklepu() {

        JTextField idSklepu = new JTextField(30);
        JTextField nowyAdres = new JTextField(30);
        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Zmiany sklepu ID:"));
        myPanel.add(idSklepu);
        myPanel.add(new JLabel("Nowy adres"));
        myPanel.add(nowyAdres);


        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Wprowadź nowy adres sklepu", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String adres = (String) nowyAdres.getText();
            int idSklepu2= Integer.parseInt(idSklepu.getText());
            updateSklep(idSklepu2, adres);
        }
    }
    private void updateSklep(int idSklepu, String p_adres) {
        String procedureCall = "{ call UPDATESKLEP(?, ?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, idSklepu);
            callableStatement.setString(2, p_adres);
            callableStatement.execute();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Błąd SQL: " + e.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void readSklepy(){
        String procedureCall = "{ call READSKLEPY(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();
            ResultSet rs = (ResultSet) callableStatement.getObject(1);
            while (rs.next()) {
                String listDetails = rs.getString("NAZWA_SKLEPU");
                marka.addItem(listDetails);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public int findSklepByName(String nazwaSklepu) {
        String procedureCall = "{ ? = call FINDSKLEPBYNAME(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, nazwaSklepu);
            callableStatement.execute();
            int idSklepu = callableStatement.getInt(1);
            return idSklepu;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    private void loadData() {
        String procedureCall = "{ call READSKLEPY(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();

            try (ResultSet rs = (ResultSet) callableStatement.getObject(1)) {
                // Przygotowanie modelu tabeli do wyświetlenia danych
                DefaultTableModel model = new DefaultTableModel(new String[]{"ID Sklepu", "Nazwa", "Adres"}, 0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("ID_SKLEPU"),
                        rs.getString("NAZWA_SKLEPU"),
                        rs.getString("ADRES"),
                    });
                }
                listaSklepowTable.setModel(model);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
