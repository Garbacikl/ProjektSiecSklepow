import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ProduktyWsklepiePanel extends JFrame {
    private Connection connectionNow;
    private int id;
    private JComboBox <String> sklep;
    private JComboBox <String> przedmiot;
    private JButton cofnijButton = new JButton("Wstecz");
    private JButton dodajButton = new JButton("Dodaj");
    private JButton updateButton = new JButton("Update");
    private JButton deleteButton = new JButton("Usun");
    private JTable listaProduktowTable;
    private JScrollPane scrollPane;

    private JComboBox<String> producenci;
    private JComboBox<String> kategorie;


    public ProduktyWsklepiePanel(int adminID, Connection connection) {
        this.connectionNow = connection;
        this.id = adminID;

        initializeLayout();
        createUIComponents();
        readPrzedmioty();
        readSklepy();
        setupListeners();
        loadData();
    }

    private void loadData() {
        String procedureCall = "{ call READPRODUKTY_SKLEPY(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();

            try (ResultSet rs = (ResultSet) callableStatement.getObject(1)) {
                // Przygotowanie modelu tabeli do wyświetlenia danych
                DefaultTableModel model = new DefaultTableModel(new String[]{"Nazwa Sklepu", "Nazwa Produktu"   ,"Nazwa Producenta", "Cena"}, 0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("NazwaSklepu"),
                        rs.getString("NazwaProduktu"),
                        rs.getString("NazwaProducenta"),
                        rs.getString("Cena")
                    });
                }
                listaProduktowTable.setModel(model);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeLayout() {
        setTitle("Produkty w sklepach");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void createUIComponents() {
        JLabel headerLabel = new JLabel("Produkty w sklepach", JLabel.CENTER);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 20));
        add(headerLabel, BorderLayout.NORTH);

        listaProduktowTable = new JTable();
        scrollPane = new JScrollPane(listaProduktowTable);
        add(scrollPane, BorderLayout.CENTER);

        przedmiot = new JComboBox<String>();
        sklep = new JComboBox<String>();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cofnijButton);
        buttonPanel.add(dodajButton); // Dodanie przycisku "Dodaj"
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
        dodajButton.addActionListener(e -> {
            pokazOknoDodawaniaProduktu(sklep, przedmiot);
        });
        updateButton.addActionListener(e -> {
            pokazOknoUpdateProduktu(sklep, przedmiot);
        });
        deleteButton.addActionListener(e -> {
            pokazOknoDeleteProduktu(sklep, przedmiot);
        });
    }

    private void pokazOknoDodawaniaProduktu(JComboBox<String> sklepIn,JComboBox<String> produktIn) {

        JTextField ilolscField = new JTextField(10);

        JPanel myPanel = new JPanel();

        myPanel.add(Box.createHorizontalStrut(15)); // odstęp
        myPanel.add(new JLabel("Sklep:"));
        myPanel.add(sklepIn);
        myPanel.add(new JLabel("Produkt:"));
        myPanel.add(produktIn);
        myPanel.add(new JLabel("Ilosc:"));
        myPanel.add(ilolscField);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Wprowadź dane produktu", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
               String sklep= (String) sklepIn.getSelectedItem();
               int sklepInt = findSklepByName(sklep);
               String produkt = (String) produktIn.getSelectedItem();
               int produktInt = findPrzedmiotByName(produkt);
               int ilosc = Integer.parseInt(ilolscField.getText());
               addNewProduct(sklepInt,produktInt,ilosc);
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
                sklep.addItem(listDetails);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void readPrzedmioty(){
        String procedureCall = "{ call READPRODUKTY(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();
            ResultSet rs = (ResultSet) callableStatement.getObject(1);
            while (rs.next()) {
                String listDetails = rs.getString("NazwaProduktu");
                przedmiot.addItem(listDetails);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public int findPrzedmiotByName(String nazwaPrzedmiotu) {
        String procedureCall = "{ ? = call FINDPRODUCTBYNAME(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, nazwaPrzedmiotu);
            callableStatement.execute();
            int idPrzedmiotu = callableStatement.getInt(1);
            return idPrzedmiotu;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
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

    private void addNewProduct(int p_id_sklepa,int p_id_produktu,int p_ilosc){
        String procedureCall = "{ call INSERTNEWPRODUKTY_SKLEPY(?,?,?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, p_id_sklepa);
            callableStatement.setInt(2, p_id_produktu);
            callableStatement.setInt(3, p_ilosc);
            callableStatement.execute();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void pokazOknoUpdateProduktu(JComboBox<String> sklep,JComboBox<String> produkt) {

        JTextField iloscField = new JTextField(30);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Sklep:"));
        myPanel.add(sklep);
        myPanel.add(new JLabel("Produkt:"));
        myPanel.add(produkt);
        myPanel.add(new JLabel("Ilosc:"));
        myPanel.add(iloscField);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Wprowadź dane produktu", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String sklepString= (String) sklep.getSelectedItem();
            int sklepInt = findSklepByName(sklepString);
            String produktString = (String) produkt.getSelectedItem();
            int produktInt = findPrzedmiotByName(produktString);
            int ilosc = Integer.valueOf(iloscField.getText());
            updateProduct(sklepInt,produktInt,ilosc);

        }
    }
    private void updateProduct(int sklep,int produkt, int ilosc){
        String procedureCall = "{ call UPDATEPRODUKT(?,?,?,?,?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, sklep);
            callableStatement.setInt(2, produkt);
            callableStatement.setInt(3, ilosc);
            callableStatement.execute();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pokazOknoDeleteProduktu(JComboBox<String> sklep,JComboBox<String> produkt) {
        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Sklep:"));
        myPanel.add(sklep);
        myPanel.add(new JLabel("Produkt:"));
        myPanel.add(produkt);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Wprowadź dane produktu", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String sklepString= (String) sklep.getSelectedItem();
            int sklepInt = findSklepByName(sklepString);
            String produktString = (String) produkt.getSelectedItem();
            int produktInt = findPrzedmiotByName(produktString);
            deleteProduct(sklepInt,produktInt);

        }
    }
    private void deleteProduct(int sklep, int produkt) {
        String procedureCall = "{ call DELETEPRODUKTSKLEP(?, ?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(2, sklep);
            callableStatement.setInt(1, produkt);
            callableStatement.execute();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}
