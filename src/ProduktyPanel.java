import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ProduktyPanel extends JFrame {
    private Connection connectionNow;
    private int id;
    private JButton cofnijButton=new JButton("Wstecz");
    private JButton dodajButton = new JButton("Dodaj");
    private JButton updateButton = new JButton("Update");
    private JButton deleteButton = new JButton("Usun");
    private JTable listaProduktowTable;
    private JComboBox <String> przedmiot;
    private JScrollPane scrollPane;


    private JComboBox <String> producenci;
    private JComboBox <String> kategorie;
    public ProduktyPanel(int adminID, Connection connection){
        this.connectionNow=connection;
        this.id=adminID;

        initializeLayout();
        createUIComponents();
        readProducenci();
        readKategorie();
        readPrzedmioty();
        setupListeners();
        loadData();
    }

    private void initializeLayout() {
        setTitle("Panel Produktow");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void createUIComponents() {
        JLabel headerLabel = new JLabel("Produkty", JLabel.CENTER);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 20));
        add(headerLabel, BorderLayout.NORTH);

        listaProduktowTable = new JTable();
        scrollPane = new JScrollPane(listaProduktowTable);
        add(scrollPane, BorderLayout.CENTER);
        przedmiot = new JComboBox<String>();
        producenci = new JComboBox<String>();
        kategorie = new JComboBox<String>();

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
        dodajButton.addActionListener(e -> {
            pokazOknoDodawaniaProduktu(kategorie, producenci);
        });
        updateButton.addActionListener(e -> {
            pokazOknoUpdateProduktu(kategorie, producenci);
        });
        deleteButton.addActionListener(e -> {
            pokazOknoDeleteProduktu(przedmiot);
        });
    }
    private void pokazOknoDodawaniaProduktu(JComboBox<String> kategorieIn,JComboBox<String> producenciIn) {

        JTextField nazwaField = new JTextField(10);
        JTextField cenaField = new JTextField(10);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Kategoria:"));
        myPanel.add(kategorieIn);
        myPanel.add(Box.createHorizontalStrut(15)); // odstęp
        myPanel.add(new JLabel("Producent:"));
        myPanel.add(producenciIn);
        myPanel.add(new JLabel("Nazwa:"));
        myPanel.add(nazwaField);
        myPanel.add(new JLabel("Cena:"));
        myPanel.add(cenaField);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Wprowadź dane produktu", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String kategoria= (String) kategorieIn.getSelectedItem();
            int kategoriaId = findKategoriaByName(kategoria);
            String producent = (String) producenciIn.getSelectedItem();
            int producentId = findProducentByName(producent);
            String nazwa = (String) nazwaField.getText();
            Float cena = Float.valueOf(cenaField.getText());
            addNewProduct(kategoriaId,producentId,nazwa,cena);

        }
    }
    private void pokazOknoUpdateProduktu(JComboBox<String> kategorieIn,JComboBox<String> producenciIn) {

        JTextField nazwaField = new JTextField(30);
        JTextField cenaField = new JTextField(30);
        JTextField idProduktu = new JTextField(30);

        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Zmiany produkt ID:"));
        myPanel.add(idProduktu);
        myPanel.add(new JLabel("Kategoria:"));
        myPanel.add(kategorieIn);
        myPanel.add(Box.createHorizontalStrut(15)); // odstęp
        myPanel.add(new JLabel("Producent:"));
        myPanel.add(producenciIn);
        myPanel.add(new JLabel("Nazwa:"));
        myPanel.add(nazwaField);
        myPanel.add(new JLabel("Cena:"));
        myPanel.add(cenaField);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Wprowadź dane produktu", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int idProduktu2= Integer.parseInt(idProduktu.getText());
            String kategoria= (String) kategorieIn.getSelectedItem();
            int kategoriaId = findKategoriaByName(kategoria);
            String producent = (String) producenciIn.getSelectedItem();
            int producentId = findProducentByName(producent);
            String nazwa = (String) nazwaField.getText();
            Float cena = Float.valueOf(cenaField.getText());
            updateProduct(idProduktu2,kategoriaId,producentId,nazwa,cena);

        }
    }
    private void addNewProduct(int p_id_kategorii,int p_id_producenta,String p_nazwa,float p_cena){
        String procedureCall = "{ call INSERTNEWPRODUKT(?,?,?,?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, p_id_kategorii);
            callableStatement.setInt(2, p_id_producenta);
            callableStatement.setString(3, p_nazwa);
            callableStatement.setFloat(4, p_cena);
            callableStatement.execute();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProduct(int idProduktu,int p_id_kategorii,int p_id_producenta,String p_nazwa,float p_cena){
        String procedureCall = "{ call UPDATEPRODUKT(?,?,?,?,?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, idProduktu);
            callableStatement.setInt(2, p_id_kategorii);
            callableStatement.setInt(3, p_id_producenta);
            callableStatement.setString(4, p_nazwa);
            callableStatement.setFloat(5, p_cena);
            callableStatement.execute();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void pokazOknoDeleteProduktu(JComboBox<String> produkt) {
        JPanel myPanel = new JPanel();
        myPanel.add(new JLabel("Produkt:"));
        myPanel.add(produkt);

        int result = JOptionPane.showConfirmDialog(null, myPanel,
            "Wprowadź dane produktu", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String produktString = (String) produkt.getSelectedItem();
            int produktInt = findProduktByName(produktString);
            deleteProduct(produktInt);

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
    public int findProduktByName(String nazwaPrzedmiotu) {
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
    private void deleteProduct(int idProduktu){
        String procedureCall = "{ call DELETEPRODUKT(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.setInt(1, idProduktu);
            callableStatement.execute();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void readProducenci(){
        String procedureCall = "{ call READPRODUCENCI(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();
            ResultSet rs = (ResultSet) callableStatement.getObject(1);
            while (rs.next()) {
                String listDetails = rs.getString("NAZWA");
                producenci.addItem(listDetails);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void readKategorie(){
        String procedureCall = "{ call READKATEGORIE(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();
            ResultSet rs = (ResultSet) callableStatement.getObject(1);
            while (rs.next()) {
                String listDetails = rs.getString("NAZWA");
                kategorie.addItem(listDetails);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public int findProducentByName(String nazwaProducenta) {
        String procedureCall = "{ ? = call FINDPRODUCENTBYNAME(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, nazwaProducenta);
            callableStatement.execute();
            int idProducenta = callableStatement.getInt(1);
            return idProducenta;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
    public int findKategoriaByName(String nazwaKategorii) {
        String procedureCall = "{ ? = call FINDKATEGORIABYNAME(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, Types.INTEGER);
            callableStatement.setString(2, nazwaKategorii);
            callableStatement.execute();
            int idKategorii = callableStatement.getInt(1);
            return idKategorii;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "SQL Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
    private void loadData() {
        String procedureCall = "{ call READPRODUKTY(?) }";
        try (CallableStatement callableStatement = connectionNow.prepareCall(procedureCall)) {
            callableStatement.registerOutParameter(1, OracleTypes.CURSOR);
            callableStatement.execute();

            try (ResultSet rs = (ResultSet) callableStatement.getObject(1)) {
                // Przygotowanie modelu tabeli do wyświetlenia danych
                DefaultTableModel model = new DefaultTableModel(new String[]{"ID Produktu", "Nazwa Produktu", "Nazwa Sklepu", "Nazwa Producenta", "Cena"}, 0);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("ID_PRODUKTU"),
                        rs.getString("NazwaProduktu"),
                        rs.getString("NazwaSklepu"),
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

}


